package com.nadi.service;

import com.nadi.model.Notification;
import com.nadi.model.Utilisateur;
import com.nadi.repository.NotificationRepository;
import com.nadi.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional(readOnly = true)
    public Page<Notification> getByUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUtilisateurIdOrderByDateEnvoiDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findByUtilisateurIdAndLuFalse(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUtilisateurIdAndLuFalse(userId);
    }

    @Transactional
    public Notification create(Utilisateur utilisateur, Notification.TypeNotification type, String message, String details) {
        Notification notification = Notification.builder()
                .utilisateur(utilisateur)
                .type(type)
                .message(message)
                .details(details)
                .build();
        Notification saved = notificationRepository.save(notification);

        pushNotificationService.sendToDevice(utilisateur, typeLabel(type), message,
                Map.of("type", type.name(), "notificationId", String.valueOf(saved.getId())));

        return saved;
    }

    private String typeLabel(Notification.TypeNotification type) {
        return switch (type) {
            case DOCUMENT_EXPIRE -> "Document expiré";
            case DOCUMENT_EXPIRE_BIENTOT -> "Document bientôt expiré";
            case PAIEMENT_EN_RETARD -> "Paiement en retard";
            case PAIEMENT_RAPPEL -> "Rappel de paiement";
            case CHANGEMENT_HORAIRE -> "Changement d'horaire";
            case COMPETITION -> "Compétition";
        };
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée: " + id));
        notif.setLu(true);
        notificationRepository.save(notif);
    }

    @Transactional
    public void markAsReadForUser(Long id, Long userId) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée: " + id));
        if (!notif.getUtilisateur().getId().equals(userId)) {
            throw new RuntimeException("Accès interdit");
        }
        notif.setLu(true);
        notificationRepository.save(notif);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUtilisateurIdAndLuFalse(userId);
        for (Notification n : unread) {
            n.setLu(true);
            notificationRepository.save(n);
        }
    }
}
