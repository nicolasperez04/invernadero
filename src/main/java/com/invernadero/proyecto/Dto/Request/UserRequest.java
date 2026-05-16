package com.invernadero.proyecto.Dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para crear o actualizar un usuario")
public class UserRequest {

    @NotBlank(message = "{validation.user.name.required}")
    @Schema(description = "Nombre del usuario", example = "Juan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "{validation.user.lastName.required}")
    @Schema(description = "Apellido del usuario", example = "Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.required}")
    @Schema(description = "Correo electrónico único", example = "juan.perez@sigma.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "{validation.user.password.required}")
    @Schema(description = "Contraseña (se encripta)", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "{validation.user.role.required}")
    @Schema(description = "Rol: ADMIN, OPERATOR, VIEWER", example = "OPERATOR", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

}
