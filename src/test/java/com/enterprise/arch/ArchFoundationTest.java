package com.enterprise.arch;

import com.enterprise.arch.aop.RequestLogAspect;
import com.enterprise.arch.auth.LoginUser;
import com.enterprise.arch.auth.LoginUserContext;
import com.enterprise.arch.exception.GlobalExceptionHandler;
import com.enterprise.arch.interceptor.AuthenticationInterceptor;
import com.enterprise.arch.interceptor.PermissionInterceptor;
import com.enterprise.arch.jwt.JwtProperties;
import com.enterprise.arch.jwt.JwtUtils;
import com.enterprise.arch.permission.PermissionChecker;
import com.enterprise.arch.permission.PermissionCodeProvider;
import com.enterprise.common.annotation.Anonymous;
import com.enterprise.common.annotation.Perm;
import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import com.enterprise.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchFoundationTest {

    @AfterEach
    void clearContext() {
        LoginUserContext.clear();
    }

    @Test
    void jwtGeneratesParsesAndRejectsInvalidToken() {
        JwtUtils jwtUtils = jwtUtils();
        String token = jwtUtils.generateToken(9L, "neo");

        LoginUser loginUser = jwtUtils.parseToken(token);

        assertThat(loginUser.getUserId()).isEqualTo(9L);
        assertThat(loginUser.getUsername()).isEqualTo("neo");
        assertThatThrownBy(() -> jwtUtils.parseToken(token + "x")).isInstanceOf(BizException.class);
    }

    @Test
    void loginUserContextSetGetAndClear() {
        LoginUserContext.set(new LoginUser(1L, "admin"));

        assertThat(LoginUserContext.getUserId()).isEqualTo(1L);
        LoginUserContext.clear();
        assertThat(LoginUserContext.get()).isNull();
    }

    @Test
    void authenticationInterceptorSkipsAnonymousAndRejectsMissingToken() throws Exception {
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor(jwtUtils());
        HandlerMethod anonymous = handlerMethod("anonymous");
        HandlerMethod secured = handlerMethod("secured");

        assertThat(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), anonymous)).isTrue();
        assertThatThrownBy(() -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), secured))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10001);
    }

    @Test
    void authenticationInterceptorAcceptsBearerTokenAndClearsContext() throws Exception {
        JwtUtils jwtUtils = jwtUtils();
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor(jwtUtils);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtUtils.generateToken(2L, "alice"));

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), handlerMethod("secured"))).isTrue();
        assertThat(LoginUserContext.getUserId()).isEqualTo(2L);
        interceptor.afterCompletion(request, new MockHttpServletResponse(), handlerMethod("secured"), null);
        assertThat(LoginUserContext.get()).isNull();
    }

    @Test
    void permissionCheckerAndInterceptorCoverPassAndDeniedBranches() throws Exception {
        PermissionCodeProvider codeProvider = userId -> Set.of("role:create");
        ObjectProvider<PermissionCodeProvider> provider = objectProvider(codeProvider);
        PermissionChecker checker = new PermissionChecker(provider);
        PermissionInterceptor interceptor = new PermissionInterceptor(checker);

        LoginUserContext.set(new LoginUser(1L, "admin"));
        assertThat(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handlerMethod("needsPerm"))).isTrue();
        assertThatThrownBy(() -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handlerMethod("needsOtherPerm")))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10003);
        assertThat(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handlerMethod("secured"))).isTrue();
    }

    @Test
    void globalExceptionHandlerMapsExpectedErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        Result<Void> biz = handler.handleBizException(new BizException(CommonErrorCode.PERMISSION_DENIED));
        Result<Void> system = handler.handleException(new RuntimeException("boom"));

        assertThat(biz.getCode()).isEqualTo(10003);
        assertThat(system.getCode()).isEqualTo(10000);
    }

    @Test
    void requestLogAspectProceedsAndReturnsResult() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("UserController.login(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"password=secret"});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = new RequestLogAspect().logRequest(joinPoint);

        assertThat(result).isEqualTo("ok");
    }

    private JwtUtils jwtUtils() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-for-jwt-utils");
        properties.setExpirationSeconds(60);
        return new JwtUtils(properties);
    }

    private HandlerMethod handlerMethod(String name) throws NoSuchMethodException {
        Method method = SampleController.class.getDeclaredMethod(name);
        return new HandlerMethod(new SampleController(), method);
    }

    private ObjectProvider<PermissionCodeProvider> objectProvider(PermissionCodeProvider codeProvider) {
        return new ObjectProvider<>() {
            @Override
            public PermissionCodeProvider getObject(Object... args) {
                return codeProvider;
            }

            @Override
            public PermissionCodeProvider getIfAvailable() {
                return codeProvider;
            }

            @Override
            public PermissionCodeProvider getIfUnique() {
                return codeProvider;
            }

            @Override
            public PermissionCodeProvider getObject() {
                return codeProvider;
            }

            @Override
            public Iterator<PermissionCodeProvider> iterator() {
                return List.of(codeProvider).iterator();
            }
        };
    }

    static class SampleController {
        @Anonymous
        void anonymous() {
        }

        void secured() {
        }

        @Perm("role:create")
        void needsPerm() {
        }

        @Perm("permission:list")
        void needsOtherPerm() {
        }
    }
}
