package org.clarkproject.aioapi.api.tool;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.clarkproject.aioapi.api.obj.RequestAccessLog;
import org.clarkproject.aioapi.api.service.AccessLogService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Component
@Aspect
@Slf4j
public class LoggerAOP {

    private final AccessLogService accessLogService;
    public LoggerAOP(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }
    // 定義 ThreadLocal 變數來存儲開始時間
    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * org.clarkproject.aioapi.api.controller.MemberControllerImpl.*(..))")
    public void pointcutMemberController() {
    }

    @Around("pointcutMemberController()")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());

        // 獲取 Request 信息
        String requestLog = getRequestLog(joinPoint);
        System.out.println("requestLog = " + requestLog);

        // 執行目標方法並獲取返回值 (即 Response)
        Object result = joinPoint.proceed();

        // 獲取 Response 信息
        String responseLog = getResponseLog(result);
        System.out.println("responseLog = " + responseLog);

        // 紀錄 AccessLog 並寫入mongoDB
        RequestAccessLog requestAccessLog = new RequestAccessLog(requestLog, responseLog, LocalDateTime.now());
        accessLogService.insertAccessLog(requestAccessLog);

        // 紀錄執行時間
        long elapsedTime = System.currentTimeMillis() - startTime.get();
        log.info("執行時間: {} ms", elapsedTime);

        return result;
    }

    private String getRequestLog(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 使用 Spring 提供的 ContentCachingRequestWrapper 包裝 HttpServletRequest (避免直接讀取InputStream)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        StringBuilder logBuilder = new StringBuilder();
        // 獲取所有的 headers
        Enumeration<String> headerNames = wrappedRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = wrappedRequest.getHeader(headerName);
            logBuilder.append(headerName).append(": ").append(headerValue).append("; ");
        }

        // 獲取 query string
        String queryString = wrappedRequest.getQueryString();
        if (queryString != null) {
            logBuilder.append("\nQueryString: ").append(queryString).append("; ");
        }

        // 獲取 RequestBody
        String requestBody = getRequestBody(wrappedRequest);
        logBuilder.append("\nRequestBody: ").append(requestBody);

        return logBuilder.toString();
    }

    private String getRequestBody(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            stringBuilder.append(reader.lines().collect(Collectors.joining(System.lineSeparator())));
        } catch (IOException e) {
            log.error("讀取 RequestBody 發生錯誤", e);
        }
        return stringBuilder.toString();
    }

    private String getResponseLog(Object result) {
        // 這裡你可以根據需求自定義回應信息的紀錄方式
        return result != null ? result.toString() : "null";
    }
}
