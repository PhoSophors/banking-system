package com.sophors.banking_system.auth;

import com.sophors.banking_system.auth.dto.LoginRequest;
import com.sophors.banking_system.user.User;
import com.sophors.banking_system.user.UserRepository;
import com.sophors.banking_system.user.UserSession;
import com.sophors.banking_system.user.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionRepository userSessionRepository;

    public User register(User user) {
        user.setRole("CUSTOMER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User login(LoginRequest loginRequest, HttpServletRequest request) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

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
}
