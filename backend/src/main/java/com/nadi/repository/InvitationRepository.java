package com.nadi.repository;

import com.nadi.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findByEmailAndStatut(String email, Invitation.StatutInvitation statut);

    List<Invitation> findByStatut(Invitation.StatutInvitation statut);

    boolean existsByEmailAndStatut(String email, Invitation.StatutInvitation statut);
}
