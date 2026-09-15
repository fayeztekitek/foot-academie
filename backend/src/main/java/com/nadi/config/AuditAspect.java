package com.nadi.config;

import com.nadi.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Pointcut("execution(* com.nadi.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        String resource = joinPoint.getTarget().getClass().getSimpleName().replace("Controller", "");
        String action = joinPoint.getSignature().getName();

        String resourceId = "";
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            resourceId = String.valueOf(args[0]);
        }

        HttpServletRequest request = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) request = attrs.getRequest();

        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "unknown";

        boolean success = true;
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            throw e;
        } finally {
            try {
                auditLogService.log(
                        method + " " + action,
                        resource,
                        resourceId,
                        uri,
                        success
                );
            } catch (Exception e) {
                log.error("Failed to write audit log", e);
            }
        }
    }
}
