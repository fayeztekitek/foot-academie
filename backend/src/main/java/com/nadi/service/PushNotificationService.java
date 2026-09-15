package com.nadi.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.nadi.model.DeviceToken;
import com.nadi.model.Utilisateur;
import com.nadi.repository.DeviceTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Value("${firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        if (!firebaseEnabled) {
            log.info("Firebase push disabled (firebase.enabled=false)");
            return;
        }
        try {
            GoogleCredentials credentials;
            ClassPathResource resource = new ClassPathResource(credentialsPath);
            if (resource.exists()) {
                credentials = GoogleCredentials.fromStream(resource.getInputStream());
            } else {
                log.warn("Firebase credentials not found: {}. Using default.", credentialsPath);
                credentials = GoogleCredentials.getApplicationDefault();
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            FirebaseApp.initializeApp(options);
            initialized = true;
            log.info("Firebase initialized successfully");
        } catch (IOException e) {
            log.warn("Firebase init failed: {}. Push disabled.", e.getMessage());
        }
    }

    public void sendToDevice(Utilisateur utilisateur, String title, String body, Map<String, String> data) {
        if (!initialized) return;
        List<DeviceToken> tokens = deviceTokenRepository.findByUtilisateurId(utilisateur.getId());
        if (tokens.isEmpty()) return;

        MulticastMessage message = MulticastMessage.builder()
                .putAllData(data != null ? data : Map.of())
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .addAllTokens(tokens.stream().map(DeviceToken::getToken).toList())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < response.getResponses().size(); i++) {
                SendResponse r = response.getResponses().get(i);
                if (!r.isSuccessful()) {
                    String error = r.getException() != null ? r.getException().getMessage() : "";
                    if (error.contains("INVALID_ARGUMENT") || error.contains("NOT_FOUND") || error.contains("UNREGISTERED")) {
                        failedTokens.add(tokens.get(i).getToken());
                    }
                }
            }
            if (!failedTokens.isEmpty()) {
                deviceTokenRepository.deleteByTokenIn(failedTokens);
                log.info("Cleaned {} invalid FCM tokens", failedTokens.size());
            }
            log.debug("Push to {}: {} success, {} failed", utilisateur.getEmail(),
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Push failed for {}: {}", utilisateur.getEmail(), e.getMessage());
        }
    }

    public void sendToAll(Utilisateur utilisateur, String title, String body) {
        sendToDevice(utilisateur, title, body, Map.of());
    }
}
