package com.crazy.rain.aop;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @ClassName: LogInterceptor
 * @Description: 记录日志
 * @author: CrazyRain
 * @date: 2024/4/18 下午5:50
 */

@Aspect
@Component
@Slf4j
public class LogInterceptor {

    @Around("execution(* com.crazy.rain.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint pjp) throws Throwable {

        //开启性能分析
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //获取本次请求对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取请求uri
        String uri = httpServletRequest.getRequestURI();
        //生成本次请求的唯一id
        String requestId = UUID.randomUUID().toString();

        Object[] args = pjp.getArgs();
        Signature signature = pjp.getSignature();
        String methodName = signature.getName();
        String requestArg = "[" + StringUtils.join(args, ",") + "]";
        //输出 日志
        log.info("Request Start: id:{}, uri:{}, methodName:{}, requestArg:{}, ip:{}",
                requestId, uri, methodName, requestArg, httpServletRequest.getRemoteAddr());
        //开始执行原方法
        Object result = pjp.proceed();
        //关闭性能分析器
        stopWatch.stop();
        log.info("request end id:{}, Time-consuming:{} ms", requestId, stopWatch.getTotalTimeMillis());
        //返回结果
        return result;
    }

}
