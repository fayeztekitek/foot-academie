package com.nadi.repository;

import com.nadi.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUtilisateurId(Long utilisateurId);

    Optional<DeviceToken> findByToken(String token);

    void deleteByUtilisateurId(Long utilisateurId);

    @Modifying
    @Query("DELETE FROM DeviceToken d WHERE d.token IN :tokens")
    void deleteByTokenIn(@Param("tokens") List<String> tokens);
}
