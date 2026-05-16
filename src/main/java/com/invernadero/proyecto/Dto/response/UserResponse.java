package com.invernadero.proyecto.Dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Respuesta con datos públicos de un usuario")
@Data
@Builder
public class UserResponse {

    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;
    @Schema(description = "Nombre del usuario", example = "Carlos")
    private String name;
    @Schema(description = "Apellido del usuario", example = "García")
    private String lastName;
    @Schema(description = "Correo electrónico", example = "carlos.garcia@ejemplo.com")
    private String email;
    @Schema(description = "Rol del usuario en el sistema", example = "ADMIN")
    private String role;
    @Schema(description = "Indica si el usuario está activo", example = "true")
    private boolean active;
}
