package com.invernadero.proyecto.Exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleValidation() {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(messageSource.getMessage(eq("error.validation.title"), isNull(), eq("error.validation.title"), any()))
                .thenReturn("Validation Error");
        when(messageSource.getMessage(eq("error.validation.message"), isNull(), eq("error.validation.message"), any()))
                .thenReturn("Invalid data");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "name", "must not be blank")
        ));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Validation Error", body.getError());
        assertEquals("Invalid data", body.getMessage());
        assertEquals("/api/test", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void handleConstraintViolation() {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(messageSource.getMessage(eq("error.constraint.title"), isNull(), eq("error.constraint.title"), any()))
                .thenReturn("Constraint Error");
        when(messageSource.getMessage(eq("error.constraint.message"), isNull(), eq("error.constraint.message"), any()))
                .thenReturn("Invalid value");

        @SuppressWarnings("unchecked")
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(null);
        when(violation.getMessage()).thenReturn("must be positive");
        Set<ConstraintViolation<?>> violations = Set.of(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<ApiError> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Constraint Error", response.getBody().getError());
    }

    @Test
    void handleBadCredentials() {
        when(request.getRequestURI()).thenReturn("/api/login");
        when(messageSource.getMessage(eq("error.unauthorized.title"), isNull(), eq("error.unauthorized.title"), any()))
                .thenReturn("Unauthorized");
        when(messageSource.getMessage(eq("error.unauthorized.message"), isNull(), eq("error.unauthorized.message"), any()))
                .thenReturn("Bad credentials");

        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiError> response = handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void handleAccessDenied() {
        when(request.getRequestURI()).thenReturn("/api/admin");
        when(messageSource.getMessage(eq("error.forbidden.title"), isNull(), eq("error.forbidden.title"), any()))
                .thenReturn("Forbidden");
        when(messageSource.getMessage(eq("error.forbidden.message"), isNull(), eq("error.forbidden.message"), any()))
                .thenReturn("Access denied");

        AccessDeniedException ex = mock(AccessDeniedException.class);

        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void handleRuntimeException() {
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(messageSource.getMessage(eq("error.business.title"), isNull(), eq("error.business.title"), any()))
                .thenReturn("Business Error");

        RuntimeException ex = new RuntimeException("Resource not found");

        ResponseEntity<ApiError> response = handler.handleRuntime(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void handleGeneralException() {
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(messageSource.getMessage(eq("error.internal.title"), isNull(), eq("error.internal.title"), any()))
                .thenReturn("Internal Error");
        when(messageSource.getMessage(eq("error.internal.message"), isNull(), eq("error.internal.message"), any()))
                .thenReturn("An unexpected error occurred");

        Exception ex = new Exception("Unexpected");

        ResponseEntity<ApiError> response = handler.handleGeneral(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
    }
}
