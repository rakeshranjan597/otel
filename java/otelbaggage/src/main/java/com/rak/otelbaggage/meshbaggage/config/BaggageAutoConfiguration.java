package com.rak.otelbaggage.meshbaggage.config;

import com.rak.otelbaggage.meshbaggage.baggage.BaggageExtractFilter;
import com.rak.otelbaggage.meshbaggage.baggage.BaggageInjectInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@AutoConfiguration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class BaggageAutoConfiguration {

    private static final Logger LOGGER = Logger.getLogger(BaggageAutoConfiguration.class.getName());

    @Value("${ENV_DOMAIN}")
    private String internalHostSuffix;

    /**
     * baggageInBoundFilter() ->
     *      Registers the {@link BaggageExtractFilter} as a global servlet filter.
     * setOrder() ->
     *      It ensures the filter runs immediately when a request arrives.
     *      Allowing the baggage to be available for every subsequent step of the request lifecycle, including security checks and business logic.
     * addUrlPatterns("/*") ->
     *      This ensures the baggage is extracted for every single API endpoint in the microservice.
     * @return {@link FilterRegistrationBean<BaggageExtractFilter>}->
     *      Spring Boot will automatically look for this return type & register the filter into the servlet container during startup.
     */
    @Bean
    public FilterRegistrationBean<BaggageExtractFilter> baggageInBoundFilter() {
        FilterRegistrationBean<BaggageExtractFilter> reg = new FilterRegistrationBean<>(new BaggageExtractFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Integer.MIN_VALUE);
        LOGGER.info("Registered the Baggage Extract with Servlet");
        return reg;
    }


    /**
     * restTemplateBaggageOutBound() ->
     *      It automatically attaches the {@link BaggageInjectInterceptor} to BeanPostProcessor.postProcessAfterInitialization if a created bean is a RestTemplate
     *      this specific method runs after the bean has been fully constructed, its dependencies have been injected, and its initial setup {@link jakarta.annotation.PostConstruct} is complete.
     * @return {@link BeanPostProcessor} ->
     *      This is a Spring "hook" that watches every bean created in the application.
     */
    @Bean
    public BeanPostProcessor restTemplateBaggageOutBound() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof RestTemplate rt) {
                    LOGGER.info("Registered the Baggage Inject with BeanPostProcessor AfterInitialization");
                    rt.getInterceptors().add(new BaggageInjectInterceptor(internalHostSuffix));
                }
                return bean;
            }
        };
    }
}