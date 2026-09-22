package com.college.babysitter.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the H2 console servlet follows the flag: on for local dev,
 * off in production (H2_CONSOLE_ENABLED=false) so a public backend
 * never exposes a database console.
 */
class H2ConsoleConfigTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(H2ConsoleConfig.class));

    @Test
    void registersServletWhenEnabled() {
        runner.withPropertyValues("spring.h2.console.enabled=true")
                .run(ctx -> assertTrue(ctx.containsBean("h2ConsoleServletRegistration")));
    }

    @Test
    void skipsServletWhenDisabled() {
        runner.withPropertyValues("spring.h2.console.enabled=false")
                .run(ctx -> assertFalse(ctx.containsBean("h2ConsoleServletRegistration")));
    }
}
