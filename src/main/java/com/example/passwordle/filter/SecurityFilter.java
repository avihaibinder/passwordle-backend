package com.example.passwordle.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final String gatewaySecret;
    private final String appApiKey;

    public SecurityFilter(
            @Value("${GATEWAY_SECRET}") String gatewaySecret,
            @Value("${APP_API_KEY}") String appApiKey) {
        this.gatewaySecret = gatewaySecret;
        this.appApiKey = appApiKey;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/health".equals(path) || "/health/".equals(path);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String gatewayHeader = request.getHeader("X-Gateway-Secret");
        String appHeader = request.getHeader("X-App-API-Key");

        if (gatewayHeader == null || !gatewayHeader.equals(gatewaySecret) ||
                appHeader == null || !appHeader.equals(appApiKey)) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\": \"Unauthorized\", \"message\": \"Invalid or missing security headers\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
