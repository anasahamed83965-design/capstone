package com.college.babysitter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the auto-generated Swagger UI (springdoc-openapi), which is the
 * project's API contract (Review-II section 7.4): it reads the controllers
 * directly, so docs never drift from the code. The bearer scheme powers the
 * "Authorize" button in the UI; paste any login JWT there to try secured
 * endpoints.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Builds the OpenAPI definition with project title, version and the
     * JWT bearer security scheme used by every secured endpoint.
     *
     * @return the OpenAPI definition
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Babysitter Booking API")
                        .description("REST API for the babysitter booking platform")
                        .version("0.1.0")
                        .contact(new Contact().name("Capstone Team")))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
