package com.danzo.multi_currency_expense_tracker.expense;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CreateExpenseRequest request
            ){
        ExpenseResponse response = expenseService.create(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM")YearMonth month
            ){
        List<ExpenseResponse> response = expenseService.list(userDetails.getUsername(),month);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<HttpStatus> delete(
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        expenseService.delete(userDetails.getUsername(), expenseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
