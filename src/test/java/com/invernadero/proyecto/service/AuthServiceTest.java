package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Repository.UserRepository;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success() {
        // Arrange
        String email = "test@test.com";
        String password = "1234";

        User user = new User();
        user.setEmail(email);

        // authenticate NO lanza excepción → éxito
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        // Act
        String token = authService.login(email, password);

        // Assert
        assertNotNull(token);
        assertEquals("jwt-token", token);

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(email);
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_userNotFound() {
        // Arrange
        String email = "test@test.com";
        String password = "1234";

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.login(email, password)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_badCredentials() {
        // Arrange
        String email = "test@test.com";
        String password = "wrong";

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.login(email, password)
        );
        assertEquals("Credenciales inválidas", exception.getMessage());

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));


        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_userNotFound_shouldThrowException() {
        String email = "noexiste@mail.com";
        String password = "123456";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.login(email, password)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());

        verify(authManager).authenticate(any());
        verify(userRepository).findByEmail(email);
        verify(jwtService, never()).generateToken(any());
    }


    @Test
    void login_badCredentials_shouldThrowException() {
        String email = "test@mail.com";
        String password = "wrong-password";

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authManager)
                .authenticate(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.login(email, password)
        );
        assertEquals("Credenciales inválidas", exception.getMessage());

        verify(authManager).authenticate(any());
        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }


    @Test
    void login_tokenGenerationFails_shouldThrowException() {
        String email = "test@mail.com";
        String password = "123456";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user))
                .thenThrow(new RuntimeException("Error generando token"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.login(email, password)
        );

        assertEquals("Error generando token", exception.getMessage());

        verify(authManager).authenticate(any());
        verify(userRepository).findByEmail(email);
        verify(jwtService).generateToken(user);
    }


}
