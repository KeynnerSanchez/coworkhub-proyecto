package com.coworkhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CoWorkHub API")
                        .version("1.0.0")
                        .description("API REST para gestionar sedes, salas, miembros y reservas de una red de coworking. "
                                + "Para probarla: 1) POST /auth/login, 2) copia el token, 3) boton Authorize."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    // ---- Grupos de Swagger ----
    @Bean
    public GroupedOpenApi grupoAutenticacion() {
        return GroupedOpenApi.builder().group("1-autenticacion").pathsToMatch("/auth/**").build();
    }

    @Bean
    public GroupedOpenApi grupoCatalogos() {
        return GroupedOpenApi.builder().group("2-catalogos")
                .pathsToMatch("/api/sedes/**", "/api/salas/**", "/api/equipamientos/**",
                        "/api/servicios-adicionales/**", "/api/planes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi grupoMiembros() {
        return GroupedOpenApi.builder().group("3-miembros").pathsToMatch("/api/miembros/**").build();
    }

    @Bean
    public GroupedOpenApi grupoReservas() {
        return GroupedOpenApi.builder().group("4-reservas")
                .pathsToMatch("/api/reservas/**", "/api/disponibilidad/**").build();
    }

    // ---- Respuestas de error con ejemplo en todos los endpoints ----
    @Bean
    public OpenApiCustomizer respuestasDeError() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(ruta -> ruta.readOperations().forEach(op -> {
                agregar(op, "400", "Datos faltantes o con formato invalido", "VALIDACION");
                agregar(op, "401", "Sin token, o token alterado o vencido", "NO_AUTENTICADO");
                agregar(op, "403", "El usuario no tiene permiso", "ACCESO_DENEGADO");
                agregar(op, "404", "El recurso no existe", "NO_ENCONTRADO");
                agregar(op, "409", "Se viola una regla de negocio o hay un duplicado", "RN-01");
            }));
        };
    }

    private void agregar(Operation operacion, String codigoHttp, String descripcion, String codigoError) {
        if (operacion.getResponses() == null) {
            operacion.setResponses(new ApiResponses());
        }
        if (operacion.getResponses().get(codigoHttp) != null) {
            return;
        }
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("error", descripcion);
        ejemplo.put("codigo", codigoError);
        ejemplo.put("timestamp", "2030-06-03T13:00:00Z");

        MediaType tipo = new MediaType().example(ejemplo);
        ApiResponse respuesta = new ApiResponse()
                .description(descripcion)
                .content(new Content().addMediaType("application/json", tipo));
        operacion.getResponses().addApiResponse(codigoHttp, respuesta);
    }
}
