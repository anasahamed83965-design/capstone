package com.college.babysitter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the auto-generated Swagger UI (springdoc-openapi), which is the
 * project's API contract (Review-II section 7.4): it reads the controllers
 * directly, so docs never drift from the code.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Builds the OpenAPI definition with project title and version.
     *
     * @return the OpenAPI definition
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Babysitter Booking API")
                .description("REST API for the babysitter booking platform")
                .version("0.1.0")
                .contact(new Contact().name("Capstone Team")));
    }
}
