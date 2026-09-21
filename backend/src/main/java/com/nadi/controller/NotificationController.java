package com.nadi.controller;

import com.nadi.model.Notification;
import com.nadi.model.Utilisateur;
import com.nadi.repository.DeviceTokenRepository;
import com.nadi.model.DeviceToken;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    @GetMapping
    public ResponseEntity<Page<Notification>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Utilisateur user = getCurrentUser();
        return ResponseEntity.ok(notificationService.getByUser(user.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        Utilisateur user = getCurrentUser();
        long count = notificationService.countUnread(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        Utilisateur user = getCurrentUser();
        notificationService.markAsReadForUser(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        Utilisateur user = getCurrentUser();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("message", "Toutes les notifications marquées comme lues"));
    }

    @PostMapping("/register-device")
    public ResponseEntity<Map<String, String>> registerDevice(@RequestBody Map<String, String> body) {
        Utilisateur user = getCurrentUser();
        String token = body.get("token");
        String platform = body.getOrDefault("platform", "web");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token requis"));
        }
        deviceTokenRepository.findByToken(token).ifPresentOrElse(
                existing -> {},
                () -> {
                    DeviceToken dt = DeviceToken.builder()
                            .utilisateur(user)
                            .token(token)
                            .platform(platform)
                            .tenantId(user.getTenantId())
                            .build();
                    deviceTokenRepository.save(dt);
                }
        );
        return ResponseEntity.ok(Map.of("message", "Appareil enregistré"));
    }

    @DeleteMapping("/unregister-device")
    public ResponseEntity<Map<String, String>> unregisterDevice(@RequestParam String token) {
        deviceTokenRepository.findByToken(token).ifPresent(deviceTokenRepository::delete);
        return ResponseEntity.ok(Map.of("message", "Appareil désenregistré"));
    }

    private Utilisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof Utilisateur) {
            return (Utilisateur) auth.getPrincipal();
        }
        return utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}
