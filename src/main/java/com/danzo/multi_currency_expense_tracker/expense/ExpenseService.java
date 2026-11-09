package com.danzo.multi_currency_expense_tracker.expense;

import com.danzo.multi_currency_expense_tracker.auth.UserRepo;
import com.danzo.multi_currency_expense_tracker.auth.Users;
import com.danzo.multi_currency_expense_tracker.common.CustomException;
import com.danzo.multi_currency_expense_tracker.fx.FxClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ExpenseService {

    private final  ExpenseRepo expenseRepo;
    private final UserRepo userRepo;
    private final FxClient fxClient;

    public ExpenseService(ExpenseRepo expenseRepo, UserRepo userRepo, FxClient fxClient) {
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
        this.fxClient = fxClient;
    }
    public ExpenseResponse create(String email,CreateExpenseRequest request){
        Users user = userRepo.findUserByEmail(email)
                .orElseThrow(()->new CustomException("User not found", HttpStatus.NOT_FOUND));
        String currency = request.currency().toUpperCase(Locale.ROOT);
        BigDecimal rate = fxClient.rateToEur(currency, request.spentOn());
        long amountEurCents = BigDecimal.valueOf(request.amountCents())
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_EVEN)
                .longValueExact();
        Expense expense = new Expense(
                user,
                request.description(),
                request.amountCents(),
                currency,
                amountEurCents,
                rate,
                request.spentOn(),
                request.category()
        );
        Expense saved = expenseRepo.save(expense);
        return ExpenseResponse.from(saved);
    }

    public List<ExpenseResponse> list (String email, YearMonth month){
        Users user = userRepo.findUserByEmail(email)
                .orElseThrow(()->new CustomException("User not found", HttpStatus.NOT_FOUND));
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return expenseRepo.findAllByUserIdAndSpentOnBetweenOrderBySpentOnDescCreatedAtDesc(
                user.getId(),
                from,
                to
        ).stream().map(ExpenseResponse::from).toList();
    }

    public void delete(String email, UUID id){
        Users user = userRepo.findUserByEmail(email)
                .orElseThrow(()->new CustomException("User not found", HttpStatus.NOT_FOUND));
        Expense expense = expenseRepo.findByIdAndUserId(id,user.getId())
                .orElseThrow(()->new CustomException("Expense not found", HttpStatus.NOT_FOUND));
        expenseRepo.delete(expense);
    }



}
