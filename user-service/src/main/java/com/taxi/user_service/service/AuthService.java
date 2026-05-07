package com.taxi.user_service.service;

import com.taxi.user_service.dto.auth.AuthResponse;
import com.taxi.user_service.dto.auth.LoginRequest;
import com.taxi.user_service.dto.auth.RegisterRequest;
import com.taxi.user_service.entity.*;
import com.taxi.user_service.repository.DriverRepository;
import com.taxi.user_service.repository.PassengerRepository;
import com.taxi.user_service.repository.UserRepository;
import com.taxi.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Long linkedId = null;

        if (request.getRole() == UserRole.PASSENGER) {
            Passenger passenger = Passenger.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .build();
            passenger = passengerRepository.save(passenger);
            linkedId = passenger.getId();

        } else if (request.getRole() == UserRole.DRIVER) {
            if (request.getLicenseNumber() == null) {
                throw new IllegalArgumentException("License number required for drivers");
            }
            Driver driver = Driver.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .licenseNumber(request.getLicenseNumber())
                    .status(DriverStatus.OFFLINE)
                    .build();
            driver = driverRepository.save(driver);
            linkedId = driver.getId();
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .passengerId(request.getRole() == UserRole.PASSENGER ? linkedId : null)
                .driverId(request.getRole() == UserRole.DRIVER ? linkedId : null)
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}