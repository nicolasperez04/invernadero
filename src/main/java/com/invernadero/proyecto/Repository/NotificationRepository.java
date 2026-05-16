package com.invernadero.proyecto.Repository;

import com.invernadero.proyecto.Entity.Notification;
import com.invernadero.proyecto.Entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    long countByReadFalse();

    boolean existsByLotIdAndType(Long lotId, NotificationType type);

    List<Notification> findByLotIdAndReadFalse(Long lotId);
}
