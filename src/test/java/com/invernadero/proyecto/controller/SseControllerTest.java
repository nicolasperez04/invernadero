package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Security.JwtAuthenticationFilter;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.SseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SseController.class)
@AutoConfigureMockMvc(addFilters = false)
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SseService sseService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void subscribe_validToken_returnsEventStream() throws Exception {
        when(jwtService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/api/sse/subscribe")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    void subscribe_invalidToken_returnsUnauthorized() throws Exception {
        when(jwtService.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/sse/subscribe")
                        .param("token", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void subscribe_noToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/sse/subscribe"))
                .andExpect(status().isUnauthorized());
    }
}
