package com.gokaycavdar.orderservice.config;

import com.gokaycavdar.orderservice.filter.RequestCorrelationFilter;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor correlationIdRequestInterceptor() {
        return requestTemplate -> {
            String correlationId = MDC.get("correlationId");

            if (StringUtils.hasText(correlationId)) {
                requestTemplate.header(RequestCorrelationFilter.CORRELATION_ID_HEADER, correlationId);
            }
        };
    }
}
