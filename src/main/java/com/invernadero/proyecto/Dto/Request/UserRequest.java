package com.invernadero.proyecto.Dto.Request;

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
public class UserRequest {

    @NotBlank(message = "{validation.user.name.required}")
    private String name;

    @NotBlank(message = "{validation.user.lastName.required}")
    private String lastName;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.required}")
    private String email;

    @NotBlank(message = "{validation.user.password.required}")
    private String password;

    @NotBlank(message = "{validation.user.role.required}")
    private String role; // ADMIN, OPERATOR, VIEWER

}
