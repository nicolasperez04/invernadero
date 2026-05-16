package com.invernadero.proyecto.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un tipo de evento en el sistema.
 * Los tipos de eventos definen categorías de acciones registrables en los lotes.
 */
@Schema(description = "Tipo de evento registrable en el sistema")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "event_types")
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del tipo de evento", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre único del tipo de evento", example = "RIEGO")
    private String name;

    @Column(nullable = false)
    @Schema(description = "Categoría a la que pertenece el tipo de evento", example = "MANTENIMIENTO")
    private String category;

}
