package com.invernadero.proyecto.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Entidad que representa un lote de cultivo en el invernadero.
 * Cada lote pertenece a un cultivo específico, tiene fechas de inicio,
 * fin y cosecha estimada.
 */
@Schema(description = "Lote de cultivo en el invernadero")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "lots")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del lote", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre identificador del lote", example = "Lote Norte A")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    @JsonIgnoreProperties("lots")
    @Schema(description = "Cultivo asociado al lote")
    private Crop crop;

    @Column(nullable = false)
    @Schema(description = "Fecha de inicio del lote (formato ISO 8601)", example = "2025-01-15T00:00:00Z")
    private Instant startDate;

    @Schema(description = "Fecha de fin del lote (formato ISO 8601)", example = "2025-04-15T00:00:00Z")
    private Instant endDate;

    @Column(name = "estimated_harvest_date")
    @Schema(description = "Fecha estimada de cosecha (formato ISO 8601)", example = "2025-04-15T00:00:00Z")
    private Instant estimatedHarvestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Estado persistido del lote: CREATED, IN_PRODUCTION, FINISHED", example = "CREATED")
    @Builder.Default
    private LotStatus status = LotStatus.CREATED;

    @OneToMany(mappedBy = "lot", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JsonIgnore
    @Schema(description = "Eventos registrados en este lote")
    private List<Event> events;

}
