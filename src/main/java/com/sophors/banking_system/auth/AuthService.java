package com.sophors.banking_system.auth;

import com.sophors.banking_system.auth.dto.LoginRequest;
import com.sophors.banking_system.user.User;
import com.sophors.banking_system.user.UserRole;
import com.sophors.banking_system.user.UserRepository;
import com.sophors.banking_system.user.UserSession;
import com.sophors.banking_system.user.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionRepository userSessionRepository;
    private final AuthenticationManager authenticationManager;

    public User register(User user) {
        return saveUserWithRole(user, UserRole.CUSTOMER, "SYSTEM");
    }

    public User createAdmin(User user) {
        return saveUserWithRole(user, UserRole.ADMIN, "SYSTEM_ADMIN");
    }

    public User login(LoginRequest loginRequest, HttpServletRequest request) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new RuntimeException("User is deleted");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new RuntimeException("User is disabled");
        }

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new RuntimeException("User is locked");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User is not active");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception exception) {
            int failedAttempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
            user.setFailedLoginAttempts(failedAttempts + 1);
            user.setUpdatedBy(user.getEmail());
            userRepository.save(user);
            throw new RuntimeException("Wrong password");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginDt(LocalDateTime.now());
        user.setUpdatedBy(user.getEmail());
        userRepository.save(user);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", context);

        // CREATE SESSION
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        DeviceDetails deviceDetails = DeviceDetailsParser.parse(userAgent);

        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setDeviceType(deviceDetails.getDeviceType());
        session.setDeviceName(deviceDetails.getDeviceName());
        session.setDeviceVersion(deviceDetails.getDeviceVersion());
        session.setOsName(deviceDetails.getOsName());
        session.setOsVersion(deviceDetails.getOsVersion());
        session.setBrowserName(deviceDetails.getBrowserName());
        session.setBrowserVersion(deviceDetails.getBrowserVersion());
        session.setUserAgent(userAgent);
        session.setIpAddress(ip);
        session.setLat(loginRequest.getLat());
        session.setLng(loginRequest.getLng());
        session.setLoginAt(LocalDateTime.now());

        userSessionRepository.save(session);

        return user;
    }

    private User saveUserWithRole(User user, UserRole role, String actor) {
        user.setRole(role.name());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedBy(actor);
        user.setUpdatedBy(actor);
        user.setPasswordChangedDt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setEnabled(true);
        user.setDeleted(false);
        return userRepository.save(user);
    }
}
