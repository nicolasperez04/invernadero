package com.invernadero.proyecto.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entidad que representa un evento registrado en un lote.
 * Los eventos pueden ser riegos, aplicaciones de fertilizante, cosechas, etc.
 */
@Schema(description = "Evento registrado en un lote del invernadero")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del evento", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    @JsonIgnoreProperties({"events", "crop"})
    @Schema(description = "Lote donde se registró el evento")
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    @Schema(description = "Tipo de evento registrado")
    private EventType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuario que registró el evento")
    private User user;

    @Column(nullable = false)
    @Schema(description = "Fecha y hora del evento (formato ISO 8601)", example = "2025-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "Descripción o notas adicionales del evento", example = "Riego automático completado")
    private String description;

    @Schema(description = "Fecha de creación del registro en BD (formato ISO 8601)", example = "2025-01-15T10:30:00Z")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

}
