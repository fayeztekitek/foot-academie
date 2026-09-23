package com.nadi.repository;

import com.nadi.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE utilisateur SET mot_de_passe_hash = :hash, must_change_password = false WHERE email = :email", nativeQuery = true)
    int updatePasswordByEmail(@Param("email") String email, @Param("hash") String hash);

    @Modifying
    @Transactional
    @Query(value = "UPDATE utilisateur SET mot_de_passe_hash = :hash, must_change_password = false WHERE email = :email AND tenant_id = :tenantId", nativeQuery = true)
    int updatePasswordByEmailAndTenantId(@Param("email") String email, @Param("hash") String hash, @Param("tenantId") Long tenantId);
}
