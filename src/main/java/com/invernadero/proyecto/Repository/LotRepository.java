package com.invernadero.proyecto.Repository;

import com.invernadero.proyecto.Entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotRepository extends JpaRepository<Lot, Long> {

    List<Lot> findByCropId(Long cropId);
}
