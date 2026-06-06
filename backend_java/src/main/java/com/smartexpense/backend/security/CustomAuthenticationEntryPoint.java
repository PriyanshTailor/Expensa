package com.smartexpense.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom authentication entry point for handling 403/401 errors
 * Provides clear error messages when authentication fails
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Unauthorized Access");
        errorDetails.put("message", "Authentication token is required. Please login first.");
        errorDetails.put("details", authException.getMessage());
        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("path", request.getRequestURI());

        response.getWriter().write(new ObjectMapper().writeValueAsString(errorDetails));
    }
}

