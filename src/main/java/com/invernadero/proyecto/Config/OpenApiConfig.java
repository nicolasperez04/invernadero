package com.invernadero.proyecto.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SIGMA - Sistema de Gestión de Invernaderos",
                version = "1.0.0",
                description = """
                        API REST para la gestión integral de un sistema de invernaderos.

                        Permite administrar cultivos, lotes, eventos y usuarios con autenticación JWT.

                        ## Autenticación
                        Utilice el endpoint `/api/auth/login` para obtener un token JWT.
                        Incluya el token en el header `Authorization: Bearer <token>` en todas las requests protegidas.

                        ## Zonas horarias
                        Todas las fechas se retornan en formato ISO 8601 con zona horaria UTC.
                        """,
                contact = @Contact(
                        name = "Soporte SIGMA",
                        email = "soporte@invernadero.com"
                ),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        )
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token de autenticación"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("SIGMA - Sistema de Gestión de Invernaderos")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión integral de un sistema de invernaderos.

                                Permite administrar cultivos, lotes, eventos y usuarios con autenticación JWT.

                                ## Autenticación
                                Utilice el endpoint `/api/auth/login` para obtener un token JWT.
                                Incluya el token en el header `Authorization: Bearer <token>` en todas las requests protegidas.

                                ## Zonas horarias
                                Todas las fechas se retornan en formato ISO 8601 con zona horaria UTC.
                                """)
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Soporte SIGMA")
                                .email("soporte@invernadero.com"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer token de autenticación")
                        )
                );
    }

}