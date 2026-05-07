package com.taxi.user_service.controller;

import com.taxi.user_service.dto.PassengerRequest;
import com.taxi.user_service.dto.DriverRequest;
import com.taxi.user_service.entity.*;
import com.taxi.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/passengers")
    public Passenger createPassenger(@Valid @RequestBody PassengerRequest request) {
        return userService.createPassenger(request);
    }

    @GetMapping("/passengers")
    public List<Passenger> getAllPassengers() {
        return userService.getAllPassengers();
    }

    @GetMapping("/passengers/{id}")
    public Passenger getPassenger(@PathVariable Long id) {
        return userService.getPassenger(id);
    }

    @PostMapping("/drivers")
    public Driver createDriver(@Valid @RequestBody DriverRequest request) {
        return userService.createDriver(request);
    }

    @GetMapping("/drivers/{id}")
    public Driver getDriver(@PathVariable Long id) {
        return userService.getDriver(id);
    }

    @PatchMapping("/drivers/{id}/status")
    public Driver updateStatus(@PathVariable Long id,
                               @RequestParam DriverStatus status) {
        return userService.updateDriverStatus(id, status);
    }

    @GetMapping("/drivers/available")
    public List<Driver> availableDrivers() {
        return userService.getAvailableDrivers();
    }


    @GetMapping("/me")
    public Object getCurrentUser(
            @RequestHeader(value = "X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role") String role) {

        if ("PASSENGER".equals(role)) {
            return userService.getPassenger(userId);
        } else {
            return userService.getDriver(userId);
        }
    }
}