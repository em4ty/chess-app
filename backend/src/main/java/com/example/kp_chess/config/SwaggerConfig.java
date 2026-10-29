package com.example.kp_chess.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER = "bearer";

    @Bean
    public OpenAPI myOpenAPI() {
        // Адрес сервера Swagger берёт из текущего запроса, поэтому работает и локально, и на хостинге
        return new OpenAPI()
                .info(new Info()
                        .title("KP Chess API")
                        .version("1.0")
                        .description("API шахматного обучающего приложения"))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
