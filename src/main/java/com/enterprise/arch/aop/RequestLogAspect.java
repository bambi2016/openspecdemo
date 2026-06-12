package com.enterprise.arch.aop;

import com.enterprise.common.util.SensitiveMaskUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    @Around("within(com.enterprise..controller..*)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        String args = SensitiveMaskUtils.maskText(Arrays.toString(joinPoint.getArgs()));
        log.info("request method={}, args={}", joinPoint.getSignature().toShortString(), args);
        Object result = joinPoint.proceed();
        log.info("response method={}, result={}", joinPoint.getSignature().toShortString(), SensitiveMaskUtils.maskText(String.valueOf(result)));
        return result;
    }
}
