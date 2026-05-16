package com.invernadero.proyecto.Security;

import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setPassword("password");
        user.setName("Test");
        user.setRole(Role.ADMIN);
        return user;
    }

    @Test
    void validateToken_withValidToken_returnsTrue() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_withValidTokenAndUserDetails_returnsTrue() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.validateToken(token, user));
    }

    @Test
    void validateToken_withExpiredToken_returnsFalse() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertFalse(jwtService.validateToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QG1haWwuY29tIn0.invalidsignature"));
    }

    @Test
    void validateToken_withNullToken_returnsFalse() {
        assertFalse(jwtService.validateToken((String) null));
    }

    @Test
    void generateToken_withUserDetails_returnsValidToken() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void getUsername_validToken_returnsEmail() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        String username = jwtService.getUsername(token);
        assertEquals("test@mail.com", username);
    }

    @Test
    void validateToken_withUserDetails_nonMatchingUsername_returnsFalse() {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        User otherUser = new User();
        otherUser.setEmail("other@mail.com");
        assertFalse(jwtService.validateToken(token, otherUser));
    }
}
