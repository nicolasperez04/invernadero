package com.invernadero.proyecto.Repository;

import com.invernadero.proyecto.Entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {

    Optional<EventType> findByName(String name);


}
