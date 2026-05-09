package com.invernadero.proyecto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invernadero.proyecto.Dto.Request.UserRequest;
import com.invernadero.proyecto.Dto.response.UserResponse;
import com.invernadero.proyecto.Security.JwtAuthenticationFilter;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_success() throws Exception {
        UserRequest request = UserRequest.builder()
                .name("Ana")
                .lastName("Lopez")
                .email("ana@mail.com")
                .password("123456")
                .role("ADMIN")
                .build();

        when(userService.createUser(request))
                .thenReturn(UserResponse.builder().id(1L).name("Ana").email("ana@mail.com").role("ADMIN").active(true).build());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_invalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_success() throws Exception {
        when(userService.getById(1L))
                .thenReturn(UserResponse.builder().id(1L).name("Ana").build());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAll_success() throws Exception {
        when(userService.getAll()).thenReturn(List.of(
                UserResponse.builder().id(1L).name("Ana").build(),
                UserResponse.builder().id(2L).name("Bob").build()
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void update_success() throws Exception {
        UserRequest request = UserRequest.builder()
                .name("Ana")
                .lastName("Lopez")
                .email("ana@mail.com")
                .password("123456")
                .role("ADMIN")
                .build();

        when(userService.updateUser(1L, request))
                .thenReturn(UserResponse.builder().id(1L).name("Ana").build());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void delete_success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
    }
}

