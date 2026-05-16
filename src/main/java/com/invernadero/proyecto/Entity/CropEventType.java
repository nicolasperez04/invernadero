package com.invernadero.proyecto.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Asociación entre un cultivo y un tipo de evento")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "crop_event_types")
public class CropEventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la asociación", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    @Schema(description = "Cultivo asociado")
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    @Schema(description = "Tipo de evento asociado")
    private EventType eventType;

}
