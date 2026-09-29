package com.nadi.controller;

import com.nadi.model.Utilisateur;
import com.nadi.security.SecurityUtils;
import com.nadi.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final SecurityUtils securityUtils;

    /**
     * Public iCalendar feed authenticated by the per-user token.
     * The token is a 256-bit random secret: unguessable, revocable by
     * regeneration, and revealing nothing about the account.
     */
    @GetMapping(value = "/feed", produces = "text/calendar")
    public ResponseEntity<String> feed(@RequestParam String token) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/calendar"))
                    .body(calendarService.buildFeed(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> currentToken() {
        Utilisateur user = securityUtils.getCurrentUserOrThrow();
        return ResponseEntity.ok(Map.of("token", user.getCalendarToken() != null ? user.getCalendarToken() : ""));
    }

    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> regenerateToken() {
        Utilisateur user = securityUtils.getCurrentUserOrThrow();
        return ResponseEntity.ok(Map.of("token", calendarService.regenerateToken(user)));
    }
}
