package com.danzo.multi_currency_expense_tracker.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<Users,UUID> {
     Optional<Users> findUserByEmail(String email);
     boolean existsByEmail(String email);
}
