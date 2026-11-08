package com.danzo.multi_currency_expense_tracker.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;


@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepo userRepo;


    public AppUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepo.findUserByEmail(username)
                .orElseThrow(()->new UsernameNotFoundException("user not found"));


        return User.builder()
                .username(username)
                .password(user.getPasswordHash())
                .roles("USER")
                .build();

    }
}
