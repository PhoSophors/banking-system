package com.sophors.banking_system.auth;

import com.sophors.banking_system.auth.dto.LoginRequest;
import com.sophors.banking_system.auth.dto.RegisterRequest;
import com.sophors.banking_system.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return authService.register(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest requestBody, HttpServletRequest request) {
        return authService.login(requestBody, request);
    }
}
