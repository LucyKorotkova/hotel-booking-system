package com.example.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String traceId = req.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_TRACE_ID_KEY, traceId);
        res.setHeader(TRACE_ID_HEADER, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }
}
