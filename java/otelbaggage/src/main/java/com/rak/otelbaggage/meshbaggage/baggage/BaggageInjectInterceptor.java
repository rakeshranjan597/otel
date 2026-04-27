package com.rak.otelbaggage.meshbaggage.baggage;

import com.rak.otelbaggage.meshbaggage.constant.BaggageConstants;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * This interceptor pulls the token back out of the "blind" baggage and
 * restores it as a proper Authorization header for internal calls, or strips it for external calls.
 */
public record BaggageInjectInterceptor(String internalHostSuffix) implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] body, ClientHttpRequestExecution exec)
            throws IOException {

        String host = req.getURI().getHost();
        String auth = BaggageContextHolder.get(BaggageConstants.INTERNAL_AUTH);

        if (auth != null && isInternalHost(host)) {
            // Re-inject for internal mesh
            req.getHeaders().set(BaggageConstants.AUTHORIZATION, auth);
            req.getHeaders().set(BaggageConstants.INTERNAL_AUTH, auth);
        } else {
            // Security: Ensure no baggage or auth leaks to third parties
            req.getHeaders().remove(BaggageConstants.BAGGAGE);
        }

        return exec.execute(req, body);
    }

    private boolean isInternalHost(String host) {
        return host != null && host.endsWith(internalHostSuffix);
    }
}