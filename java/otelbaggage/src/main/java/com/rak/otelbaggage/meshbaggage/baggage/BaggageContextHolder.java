package com.rak.otelbaggage.meshbaggage.baggage;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;

/**
 * Wrapper using the native OTel context provided by the DD agent.
 * No custom ThreadLocal = zero extra memory overhead for context storage.
 */
public final class BaggageContextHolder {

    /**
     *  Prevents anyone from creating an instance of this class.
     */
    private BaggageContextHolder() {}

    /**
     * This is an OTel method that retrieves the "active" context for the current thread of execution.
     * DD_TRACE_OTEL_ENABLED=true set, the Datadog agent ensures that even if your request jumps between different
     *      threads (asynchronous processing), the baggage data stays attached to the correct request.
     */
    public static Baggage current() {
        return Baggage.fromContext(Context.current());
    }

    /**
     *  Helper to extract specific keys like "Authorization"
     */
    public static String get(String key) {
        return current().getEntryValue(key);
    }
}
