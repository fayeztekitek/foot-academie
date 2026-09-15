package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponse {

    private Long id;
    private String token;
    private String email;
    private String role;
    private String invitedByEmail;
    private String statut;
    private LocalDateTime dateAcceptation;
    private LocalDateTime dateExpiration;
    private LocalDateTime createdAt;
}
