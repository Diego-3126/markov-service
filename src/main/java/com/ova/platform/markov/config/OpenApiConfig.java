package com.ova.platform.markov.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Markov Service API")
                        .description("Microservicio para generación de texto con Cadenas de Markov via JNI")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo OVA")
                                .email("equipo@ovaplatform.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Servidor Local"),
                        new Server()
                                .url("http://ec2-ip:8083")
                                .description("Servidor EC2")
                ));
    }
}