package com.example.kp_chess.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://yourdomain.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("your@email.com");
        contact.setName("Your Name");
        contact.setUrl("https://yourwebsite.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("KP Chess API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for KP Chess application")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}