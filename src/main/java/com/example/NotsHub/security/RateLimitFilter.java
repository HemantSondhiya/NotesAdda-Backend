package com.example.NotsHub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final long STALE_ENTRY_AGE_MS = WINDOW.multipliedBy(2).toMillis();
    private static final int SIGNIN_LIMIT = 10;
    private static final int SIGNUP_LIMIT = 5;
    private static final int OTP_LIMIT = 5;
    private static final int FORGOT_PASSWORD_LIMIT = 5;
    private static final int SIGNUP_STATUS_LIMIT = 20;
    private static final int NOTES_UPLOAD_LIMIT = 30;
    private static final int MAX_COUNTERS = 10_000;
    private static final int CLEANUP_EVERY_N_REQUESTS = 200;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong(0);

    @Value("${app.security.trust-forward-headers:false}")
    private boolean trustForwardHeaders;

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        int limit = 0;
        if ("POST".equalsIgnoreCase(method) && "/api/auth/signin".equals(path)) {
            limit = SIGNIN_LIMIT;
        } else if ("POST".equalsIgnoreCase(method) && "/api/auth/signup".equals(path)) {
            limit = SIGNUP_LIMIT;
        } else if ("POST".equalsIgnoreCase(method) && "/api/auth/verify-email-otp".equals(path)) {
            limit = OTP_LIMIT;
        } else if ("POST".equalsIgnoreCase(method) && "/api/auth/resend-email-otp".equals(path)) {
            limit = OTP_LIMIT;
        } else if ("POST".equalsIgnoreCase(method) && "/api/auth/forgot-password".equals(path)) {
            limit = FORGOT_PASSWORD_LIMIT;
        } else if ("GET".equalsIgnoreCase(method) && "/api/auth/signup-status".equals(path)) {
            limit = SIGNUP_STATUS_LIMIT;
        } else if ("POST".equalsIgnoreCase(method) &&
                ("/api/notes".equals(path) || "/api/notes/upload".equals(path))) {
            limit = NOTES_UPLOAD_LIMIT;
        }

        if (limit > 0) {
            String clientKey = extractClientKey(request) + ":" + method + ":" + path;
            if (!allowRequest(clientKey, limit)) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"message\":\"Too many requests\",\"status\":false,\"data\":null}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean allowRequest(String key, int limit) {
        long now = System.currentTimeMillis();

        maybeCleanup(now);
        enforceCapacity(now, key);

        Counter counter = counters.computeIfAbsent(key, k -> new Counter(now, new AtomicInteger(0)));

        synchronized (counter) {
            if (now - counter.windowStart > WINDOW.toMillis()) {
                counter.windowStart = now;
                counter.count.set(0);
            }
            counter.lastAccess = now;
            return counter.count.incrementAndGet() <= limit;
        }
    }

    private void maybeCleanup(long now) {
        if (requestCount.incrementAndGet() % CLEANUP_EVERY_N_REQUESTS != 0) {
            return;
        }
        counters.entrySet().removeIf(entry -> now - entry.getValue().lastAccess > STALE_ENTRY_AGE_MS);
    }

    private void enforceCapacity(long now, String currentKey) {
        if (counters.size() < MAX_COUNTERS || counters.containsKey(currentKey)) {
            return;
        }

        counters.entrySet().removeIf(entry -> now - entry.getValue().lastAccess > STALE_ENTRY_AGE_MS);
        while (counters.size() >= MAX_COUNTERS) {
            String keyToRemove = counters.keySet().stream().findFirst().orElse(null);
            if (keyToRemove == null) {
                break;
            }
            counters.remove(keyToRemove);
        }
    }

    private String extractClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "user:" + authentication.getName();
        }

        if (trustForwardHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static class Counter {
        private long windowStart;
        private volatile long lastAccess;
        private final AtomicInteger count;

        private Counter(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.lastAccess = windowStart;
            this.count = count;
        }
    }
}
