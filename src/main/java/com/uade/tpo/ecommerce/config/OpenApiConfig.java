package com.uade.tpo.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI 3 + Swagger UI ({@code /swagger-ui.html}).
 * <p>La seguridad real la define Spring Security; acá solo documentamos el JWT para el botón <i>Authorize</i>.</p>
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_BEARER = "bearer-jwt";

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-commerce API")
                        .description("""
                                API tipo marketplace (catálogo, carrito, pedidos, pagos mock, direcciones, reseñas, favoritos).

                                **Autenticación:** en la mayoría de rutas protegidas enviar `Authorization: Bearer <token>` \
                                obtenido en `POST /api/auth/login`.

                                **Roles:** `USER` (comprador/vendedor de sus publicaciones) y `ADMIN` (listado de usuarios, \
                                transiciones de pedido amplias, etc.).""")
                        .version("0.0.1")
                        .contact(new Contact().name("TPO API — UADE")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_BEARER,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT devuelto por POST /api/auth/login")));
    }
}
