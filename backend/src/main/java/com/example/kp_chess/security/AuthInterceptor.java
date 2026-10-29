package com.example.kp_chess.security;

import com.example.kp_chess.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Требует заголовок "Authorization: Bearer <token>" и кладёт id пользователя в атрибут запроса. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID_ATTR = "authUserId";

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        Long userId = null;
        if (header != null && header.startsWith("Bearer ")) {
            userId = tokenService.verify(header.substring(7).trim());
        }
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        request.setAttribute(USER_ID_ATTR, userId);
        return true;
    }
}
