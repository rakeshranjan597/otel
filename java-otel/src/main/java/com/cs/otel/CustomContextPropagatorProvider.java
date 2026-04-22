/**
 * @author Rakesh Ranjan
 */

package com.cs.otel;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;

/**
 *  OpenTelemetry uses Java's SPI (Service Provider Interface) to automatically discover the custom code
 *  at startup and must write a simple factory class to register the propagator
 */
public class CustomContextPropagatorProvider implements ConfigurablePropagatorProvider {

    @Override
    public TextMapPropagator getPropagator(ConfigProperties config) {
        // Returns a new instance of your logic class
        return new CustomContextPropagator();
    }

    @Override
    public String getName() {
        // This is the magic string used in OTEL_PROPAGATORS
        // This is the magic word you will use in your environment variables!
        return "java-propagator";
    }
}