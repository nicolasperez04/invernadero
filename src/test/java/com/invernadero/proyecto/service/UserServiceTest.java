package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Dto.Request.UserRequest;
import com.invernadero.proyecto.Dto.response.UserResponse;
import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Entity.enums.Role;
import com.invernadero.proyecto.Repository.UserRepository;
import com.invernadero.proyecto.Service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        UserRequest request = UserRequest.builder()
                .name("Ana")
                .lastName("Lopez")
                .email("ana@mail.com")
                .password("123456")
                .role("admin")
                .build();

        when(passwordEncoder.encode("123456")).thenReturn("encoded");

        User saved = User.builder()
                .id(1L)
                .name("Ana")
                .lastName("Lopez")
                .email("ana@mail.com")
                .password("encoded")
                .role(Role.ADMIN)
                .active(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ADMIN", response.getRole());
        assertTrue(response.isActive());
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void getById_success() {
        User user = User.builder().id(1L).name("Ana").lastName("Lopez").email("ana@mail.com").role(Role.ADMIN).active(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(1L);

        assertEquals("Ana", response.getName());
    }

    @Test
    void getById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getById(1L));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getAll_success() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(1L).name("Ana").lastName("Lopez").email("ana@mail.com").role(Role.ADMIN).active(true).build(),
                User.builder().id(2L).name("Bob").lastName("Diaz").email("bob@mail.com").role(Role.OPERATOR).active(true).build()
        ));

        List<UserResponse> response = userService.getAll();

        assertEquals(2, response.size());
    }

    @Test
    void updateUser_success_fullUpdate() {
        User user = User.builder()
                .id(1L)
                .name("Old")
                .lastName("OldL")
                .email("old@mail.com")
                .password("oldpass")
                .role(Role.OPERATOR)
                .active(true)
                .build();

        UserRequest request = UserRequest.builder()
                .name("New")
                .lastName("NewL")
                .email("new@mail.com")
                .password("newpass")
                .role("viewer")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("New", response.getName());
        assertEquals("VIEWER", response.getRole());
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void updateUser_partialUpdate() {
        User user = User.builder()
                .id(1L)
                .name("Old")
                .lastName("OldL")
                .email("old@mail.com")
                .password("oldpass")
                .role(Role.OPERATOR)
                .active(true)
                .build();

        UserRequest request = UserRequest.builder()
                .email("new@mail.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("new@mail.com", response.getEmail());
        assertEquals("OPERATOR", response.getRole());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateUser(1L, UserRequest.builder().build()));
    }

    @Test
    void deleteUser_success() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}

