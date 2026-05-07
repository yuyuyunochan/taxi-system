package com.taxi.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/debug")
public class DebugController {

    @GetMapping("/headers")
    public Map<String, String> getHeaders(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-User-Id", userId);
        headers.put("X-Username", username);
        headers.put("X-User-Role", role);
        return headers;
    }
}