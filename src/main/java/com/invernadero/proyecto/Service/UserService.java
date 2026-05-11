package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Dto.Request.UserRequest;
import com.invernadero.proyecto.Dto.response.UserResponse;
import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Entity.enums.Role;
import com.invernadero.proyecto.Repository.UserRepository;
import com.invernadero.proyecto.mapper.UserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio para la gestión de usuarios.
 * Maneja operaciones CRUD de usuarios, incluyendo creación con password encriptado.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario en el sistema.
     * Encripta la contraseña antes de almacenarla y asigna el rol especificado.
     *
     * @param request datos del usuario a crear (nombre, apellido, email, contraseña, rol)
     * @return los datos del usuario creado
     * @throws RuntimeException si el email ya está registrado en el sistema
     */
    public UserResponse createUser(UserRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = User.builder()
                .name(request.getName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .active(true)
                .build();

        return UserMapper.toDTO(userRepository.save(user));
    }

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param id identificador único del usuario
     * @return los datos del usuario encontrado
     * @throws RuntimeException si no se encuentra un usuario con el ID especificado
     */
    public UserResponse getById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserMapper.toDTO(user);
    }

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     *
     * @return lista de todos los usuarios
     */
    public List<UserResponse> getAll() {

        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    /**
     * Actualiza los datos de un usuario existente.
     * Solo actualiza los campos proporcionados que no sean nulos.
     * Si se incluye una nueva contraseña, la encripta antes de guardarla.
     *
     * @param id      identificador del usuario a actualizar
     * @param request datos a actualizar (nombre, apellido, email, contraseña, rol)
     * @return los datos del usuario actualizado
     * @throws RuntimeException si no se encuentra un usuario con el ID especificado
     */
    public UserResponse updateUser(Long id, UserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }

        return UserMapper.toDTO(userRepository.save(user));
    }

    /**
     * Elimina un usuario por su identificador.
     *
     * @param id identificador del usuario a eliminar
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}