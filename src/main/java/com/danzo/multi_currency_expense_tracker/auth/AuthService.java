package com.danzo.multi_currency_expense_tracker.auth;

import com.danzo.multi_currency_expense_tracker.common.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
   private final UserRepo userRepo;
   private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest registerRequest){
        String email = registerRequest.email().toLowerCase().trim();
        if(userRepo.existsByEmail(email)){
            throw new CustomException("email exists", HttpStatus.CONFLICT);
        }
        Users user = new Users();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        Users saved = userRepo.save(user);
        return new RegisterResponse(saved.getEmail(),saved.getId());
    }

}
