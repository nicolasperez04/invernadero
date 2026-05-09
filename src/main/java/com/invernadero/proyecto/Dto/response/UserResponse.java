package com.invernadero.proyecto.Dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String role;
    private boolean active;
}
