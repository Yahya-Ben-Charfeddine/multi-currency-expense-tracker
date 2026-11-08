package com.danzo.multi_currency_expense_tracker.auth;

import com.danzo.multi_currency_expense_tracker.common.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
   private final UserRepo userRepo;
   private final PasswordEncoder passwordEncoder;
   private final AuthenticationManager authenticationManager;
   private final JwtService jwtService;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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

    public LoginResponse login(LoginRequest request){
        String email = request.email().toLowerCase().trim();
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        return new LoginResponse(jwtService.generateToken(email));

    }

}
