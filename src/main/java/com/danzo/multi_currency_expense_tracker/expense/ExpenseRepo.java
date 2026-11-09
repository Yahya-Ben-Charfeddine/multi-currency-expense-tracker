package com.danzo.multi_currency_expense_tracker.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepo extends JpaRepository<Expense, UUID> {

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    List<Expense> findAllByUserIdAndSpentOnBetweenOrderBySpentOnDesc(UUID userId, LocalDate from, LocalDate to);
}
