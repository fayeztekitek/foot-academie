package com.nadi.repository;

import com.nadi.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUtilisateurIdOrderByDateEnvoiDesc(Long utilisateurId, Pageable pageable);

    List<Notification> findByUtilisateurIdAndLuFalse(Long utilisateurId);

    long countByUtilisateurIdAndLuFalse(Long utilisateurId);
}
