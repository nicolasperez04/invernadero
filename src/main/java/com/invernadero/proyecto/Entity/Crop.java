package com.invernadero.proyecto.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer inactivityDaysThreshold;

    @Column(nullable = false)
    private Integer estimatedGrowthDays;



    @OneToMany(
            mappedBy = "crop", cascade = CascadeType.ALL, orphanRemoval = true

    )
    @JsonIgnore
    private List<Lot> lots;

}
