package com.enterprise.arch.interceptor;

import com.enterprise.arch.auth.LoginUser;
import com.enterprise.arch.auth.LoginUserContext;
import com.enterprise.arch.jwt.JwtUtils;
import com.enterprise.common.annotation.Anonymous;
import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;

    public AuthenticationInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod) || isAnonymous(handlerMethod)) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new BizException(CommonErrorCode.TOKEN_EMPTY);
        }
        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw new BizException(CommonErrorCode.TOKEN_INVALID);
        }
        LoginUser loginUser = jwtUtils.parseToken(authorization.substring(BEARER_PREFIX.length()));
        LoginUserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }

    private boolean isAnonymous(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(Anonymous.class)
                || handlerMethod.getBeanType().isAnnotationPresent(Anonymous.class);
    }
}
