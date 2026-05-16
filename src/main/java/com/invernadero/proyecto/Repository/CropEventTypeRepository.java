package com.invernadero.proyecto.Repository;

import com.invernadero.proyecto.Entity.CropEventType;
import com.invernadero.proyecto.Entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CropEventTypeRepository extends JpaRepository<CropEventType, Long> {

    List<CropEventType> findByCropId(Long cropId);

    boolean existsByCropIdAndEventTypeId(Long cropId, Long eventTypeId);

    void deleteByCropIdAndEventTypeId(Long cropId, Long eventTypeId);

}
