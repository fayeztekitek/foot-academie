package com.nadi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${mail.from:Nadi Academie <noreply@nadi.tn>}")
    private String fromEmail;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Async
    public void sendParentCredentials(String toEmail, String prenom, String nom, String email, String motDePasse) {
        if (!mailEnabled || mailSender == null) {
            log.info("Email disabled — skipping credential email to {}", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Bienvenue sur Nadi — Vos identifiants de connexion");
            message.setText(buildParentCredentialsBody(prenom, nom, email, motDePasse));
            mailSender.send(message);
            log.info("Credentials email sent to {} for parent {} {}", toEmail, prenom, nom);
        } catch (Exception e) {
            log.error("Failed to send credentials email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildParentCredentialsBody(String prenom, String nom, String email, String motDePasse) {
        return String.format(
            "Bonjour %s %s,\n\n" +
            "Votre compte a été créé sur l'application Nadi Académie de Football.\n\n" +
            "Voici vos identifiants de connexion :\n\n" +
            "  Email : %s\n" +
            "  Mot de passe : %s\n\n" +
            "Pour des raisons de sécurité, vous devrez changer votre mot de passe lors de votre première connexion.\n\n" +
            "Connectez-vous sur : https://frontend-theta-navy-p2kodej171.vercel.app/login\n\n" +
            "Cordialement,\n" +
            "L'équipe Nadi Académie",
            prenom, nom, email, motDePasse
        );
    }
}
