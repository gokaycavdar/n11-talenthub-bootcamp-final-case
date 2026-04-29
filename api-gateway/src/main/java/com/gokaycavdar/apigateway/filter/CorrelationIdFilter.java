package com.gokaycavdar.apigateway.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;
        response.setHeader(CORRELATION_ID_HEADER, finalCorrelationId);

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                    return finalCorrelationId;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                    return Collections.enumeration(Collections.singletonList(finalCorrelationId));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> headerNames = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
                headerNames.add(CORRELATION_ID_HEADER);
                return Collections.enumeration(headerNames);
            }
        };

        filterChain.doFilter(wrappedRequest, response);
    }
}
