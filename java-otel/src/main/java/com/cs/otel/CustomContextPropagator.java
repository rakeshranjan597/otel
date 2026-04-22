/**
 * @author Rakesh Ranjan
 */

package com.cs.otel;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.Collection;
import java.util.Collections;

/**
 *  fields() ->
 *      This tells OpenTelemetry which HTTP headers this specific propagator is allowed to touch.
 *  extract() (The "Inbound" Phase) ->
 *      Read the inbound request's header & save it to the OTel Context memory. Pass Baggage through.
 *      When Service receives an incoming request, OTel pauses the request and runs this method.
 *      Now, even if the application code does 10 minutes of database work, that token is safely
 *        stored in the background memory of that specific thread.
 *          carrier: This is the incoming HTTP Request object.
 *          getter: A tool that safely reads headers from the request.
 *          context: This is the invisible "Thread Memory" (ThreadLocal) that OTel maintains while
 *                   your application processes the request.
 *  inject() (The "Outbound" Phase) ->
 *      Read the token from the OTel Context memory & write it as new-auth: <token> on the outbound request.
 *      When Service A finishes its work and makes a new HTTP call to Service B, OTel pauses the
 *        outgoing request and runs this method.
 *          context: The memory where you saved the token during extract.
 *          carrier: The new outgoing HTTP Request.
 *          setter: A tool to write headers into the outgoing request.
 */
public class CustomContextPropagator implements TextMapPropagator {

    //    @Value("${ENV_DOMAIN}")
    private String hostSuffix = "abc.corp";

    // The invisible memory key where we store the token
    private static final ContextKey<String> CS_AUTH_KEY = ContextKey.named("cs.propagate.auth.token");

    @Override
    public Collection<String> fields() {
        // Whitelist the headers we are allowed to touch
        return Collections.singletonList("Authorization");
    }

    @Override
    public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
        // 1. Look for incoming "Authorization" header
        String token = getter.get(carrier, "Authorization");

        // 2. If we found a token, save it to OTel's internal memory
        if (token != null && !token.isEmpty()) context = context.with(CS_AUTH_KEY, token);

        // 3. Extract Baggage (Standard)
        return context;
    }

    @Override
    public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
        // 1. Retrieve the token from OTel's internal memory
        String token = context.get(CS_AUTH_KEY);

        // 2. If the token is null / empty return
        if (token == null || token.isEmpty()) return;

        if (authPropagationAllowedDomain(carrier)) {
            // 3. If domain allowed & token exists, inject it into the outbound request as "new-auth"
            setter.set(carrier, "Authorization", token);
            setter.set(carrier, "New-Authorization", token);
        }
    }

    private <C> boolean authPropagationAllowedDomain(C carrier) {
        return carrier != null && carrier.toString().toLowerCase().contains(hostSuffix);
    }
}
