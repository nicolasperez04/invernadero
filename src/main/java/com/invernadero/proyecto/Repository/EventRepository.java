package com.invernadero.proyecto.Repository;

import com.invernadero.proyecto.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {


    List<Event> findByLotId(Long lotId);

    // Historial ordenado
    List<Event> findByLotIdOrderByTimestampAsc(Long lotId);

    // Validaciones de negocio
    boolean existsByLotIdAndTypeName(Long lotId, String typeName);

    // Obtener último evento
    Optional<Event> findTopByLotIdOrderByTimestampDesc(Long lotId);

    // Obtener último evento de un tipo específico para un lote
    @Query("SELECT e FROM Event e WHERE e.lot.id = :lotId AND e.type.id = :typeId ORDER BY e.timestamp DESC")
    Optional<Event> findTopByLotIdAndTypeIdOrderByTimestampDesc(@Param("lotId") Long lotId, @Param("typeId") Long typeId);


    @Query("""
        SELECT e FROM Event e
        WHERE e.lot.id = :lotId
        AND (:type IS NULL OR e.type.name = :type)
        AND (:startDate IS NULL OR e.timestamp >= :startDate)
        AND (:endDate IS NULL OR e.timestamp <= :endDate)
        ORDER BY e.timestamp ASC
    """)
    List<Event> filterEvents(Long lotId, String type, Instant startDate, Instant endDate);

    List<Event> findByLotIdOrderByTimestampDesc(Long lotId);

    @Query("""
    SELECT DATE(e.timestamp), COUNT(e)
    FROM Event e
    WHERE e.timestamp >= :startDate
    GROUP BY DATE(e.timestamp)
    ORDER BY DATE(e.timestamp)
""")
    List<Object[]> countEventsByDay(@Param("startDate") Instant startDate);

    @Query("""
    SELECT DATE(e.timestamp), COUNT(e)
    FROM Event e
    WHERE e.timestamp >= :startDate
    AND (:cropId IS NULL OR e.lot.crop.id = :cropId)
    GROUP BY DATE(e.timestamp)
    ORDER BY DATE(e.timestamp)
""")
    List<Object[]> countEventsByDay(
            @Param("startDate") Instant startDate,
            @Param("cropId") Long cropId
    );
}
