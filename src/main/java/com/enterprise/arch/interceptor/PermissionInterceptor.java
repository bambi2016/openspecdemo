package com.enterprise.arch.interceptor;

import com.enterprise.arch.auth.LoginUser;
import com.enterprise.arch.auth.LoginUserContext;
import com.enterprise.arch.permission.PermissionChecker;
import com.enterprise.common.annotation.Perm;
import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionChecker permissionChecker;

    public PermissionInterceptor(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Perm perm = resolvePerm(handlerMethod);
        if (perm == null) {
            return true;
        }
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            throw new BizException(CommonErrorCode.TOKEN_EMPTY);
        }
        if (!permissionChecker.hasPermission(loginUser.getUserId(), perm.value())) {
            throw new BizException(CommonErrorCode.PERMISSION_DENIED);
        }
        return true;
    }

    private Perm resolvePerm(HandlerMethod handlerMethod) {
        Perm methodPerm = handlerMethod.getMethodAnnotation(Perm.class);
        if (methodPerm != null) {
            return methodPerm;
        }
        return handlerMethod.getBeanType().getAnnotation(Perm.class);
    }
}
