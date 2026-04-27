package com.rak.otelbaggage.meshbaggage.baggage;

import com.rak.otelbaggage.meshbaggage.constant.BaggageConstants;
import io.opentelemetry.api.baggage.Baggage;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * This filter extracts the Authorization header and stores it in the OTel Baggage.
 * Since DD_TRACE_OTEL_ENABLED=true is set, the Datadog agent will bridge this context across threads automatically.
 */
public class BaggageExtractFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        String auth = httpReq.getHeader(BaggageConstants.AUTHORIZATION);

        if (auth != null) {
            // "Blind" storage: DD agent carries this but doesn't index it as a tag
            Baggage baggage = Baggage.current().toBuilder()
                    .put(BaggageConstants.INTERNAL_AUTH, auth)
                    .build();

            try (var ignored = baggage.makeCurrent()) {
                chain.doFilter(req, resp);
            }
        } else {
            chain.doFilter(req, resp);
        }
    }
}