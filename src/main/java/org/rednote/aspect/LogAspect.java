package org.rednote.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 日志切面
 */

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class LogAspect {

    @Pointcut("execution(* org.rednote.service..*(..))")
    public void logPointCut() {}

    @Before("logPointCut()")
    public void beforeOperation(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("方法 {} 开始执行，参数：{}", methodName, Arrays.toString(args));
    }

    @AfterReturning(value = "logPointCut()", returning = "result")
    public void afterOperation(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("方法 {} 执行完毕，返回值：{}", methodName, result);
    }

    @AfterThrowing(value = "logPointCut()", throwing = "exception")
    public void afterException(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        log.error("方法 {} 执行执行异常", methodName, exception);
    }

}
