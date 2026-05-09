package com.invernadero.proyecto.mapper;


import com.invernadero.proyecto.Dto.response.UserResponse;
import com.invernadero.proyecto.Entity.User;

public class UserMapper {

    public static UserResponse toDTO(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }

}
