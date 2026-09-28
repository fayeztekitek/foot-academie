package com.nadi.service;

import com.nadi.model.Notification;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.NotificationRepository;
import com.nadi.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PushNotificationService pushNotificationService;

    private NotificationService service() {
        return new NotificationService(notificationRepository, utilisateurRepository, pushNotificationService);
    }

    private Notification notification(Long ownerId) {
        Utilisateur owner = Utilisateur.builder()
                .id(ownerId).email("o@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
        return Notification.builder().id(1L).utilisateur(owner)
                .type(Notification.TypeNotification.PAIEMENT_RAPPEL)
                .message("Rappel").build();
    }

    @Test
    void ownerCanMarkOwnNotificationRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification(42L)));

        service().markAsReadForUser(1L, 42L);

        verify(notificationRepository).save(any());
    }

    @Test
    void strangerCannotMarkOthersNotificationRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification(99L)));

        assertThrows(RuntimeException.class, () -> service().markAsReadForUser(1L, 42L));
    }

    @Test
    void markMissingNotificationThrows() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service().markAsReadForUser(999L, 42L));
    }

    @Test
    void serializedNotificationNeverLeaksPasswordHash() throws Exception {
        // GET /notifications returns Notification entities embedding their
        // Utilisateur: the hash must never reach the client (offline brute
        // force if ever captured via XSS, logs or backups).
        Notification notification = notification(42L);
        notification.getUtilisateur().setMotDePasseHash("SECRET-HASH");
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(notification);

        assertFalse(json.contains("motDePasseHash"));
        assertFalse(json.contains("SECRET-HASH"));
        assertTrue(json.contains("o@nadi.tn"));
    }
}
