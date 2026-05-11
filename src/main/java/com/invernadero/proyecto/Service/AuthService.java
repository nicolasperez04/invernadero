package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Repository.UserRepository;
import com.invernadero.proyecto.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación para el sistema SIGMA.
 * Maneja el login de usuarios y la generación de tokens JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Autentica un usuario con email y contraseña, y genera un token JWT.
     *
     * @param email    dirección de correo electrónico del usuario
     * @param password contraseña del usuario
     * @return token JWT generado para la sesión autenticada
     * @throws RuntimeException si las credenciales son inválidas o el usuario no existe
     */
    public String login(String email, String password) {

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Credenciales inválidas");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return jwtService.generateToken(user);
    }

}