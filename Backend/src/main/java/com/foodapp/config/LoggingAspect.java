package com.foodapp.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * AOP Aspect for cross-cutting logging concerns.
 *
 * - Service layer: logs method entry with args, successful exit with result, and exceptions.
 * - Controller layer: logs HTTP method, request URI, and response outcome.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // ── Service Layer Logging ────────────────────────────────────────────────

    /**
     * Intercepts all methods in the service package.
     * Logs entry (with arguments), exit (with return value), and any thrown exception.
     */
    @Around("execution(* com.foodapp.service..*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        log.debug("[SERVICE] >> {}.{}() called with args: {}",
                className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;

            log.debug("[SERVICE] << {}.{}() completed in {}ms | result: {}",
                    className, methodName, elapsed, result);
            return result;

        } catch (Exception ex) {
            log.error("[SERVICE] !! {}.{}() threw exception: {} - {}",
                    className, methodName, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    // ── Controller Layer Logging ─────────────────────────────────────────────

    /**
     * Intercepts all methods in the controller package.
     * Logs HTTP method, request URI, and response status.
     */
    @Around("execution(* com.foodapp.controller..*(..))")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        String httpMethod = "UNKNOWN";
        String requestUri = "UNKNOWN";

        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                httpMethod = request.getMethod();
                requestUri = request.getRequestURI();
            }
        } catch (Exception ignored) {
            // Non-HTTP context — skip request details
        }

        log.info("[CONTROLLER] >> [{} {}] -> {}.{}()",
                httpMethod, requestUri, className, methodName);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;

            log.info("[CONTROLLER] << [{} {}] completed in {}ms",
                    httpMethod, requestUri, elapsed);
            return result;

        } catch (Exception ex) {
            log.error("[CONTROLLER] !! [{} {}] threw exception: {} - {}",
                    httpMethod, requestUri, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
