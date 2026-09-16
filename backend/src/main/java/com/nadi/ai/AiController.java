package com.nadi.ai;

import com.nadi.security.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;
    private final DocumentParser documentParser;
    private final SecurityUtils securityUtils;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AiController(AiService aiService, DocumentParser documentParser, SecurityUtils securityUtils) {
        this.aiService = aiService;
        this.documentParser = documentParser;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AiService.AiResponse> chat(@RequestBody ChatRequest request) {
        String userId = getCurrentUserId();
        AiService.AiResponse response = aiService.chat(userId, request.message(), request.page());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120000L);
        String userId = getCurrentUserId();

        executor.execute(() -> {
            try {
                AiService.AiResponse response = aiService.chat(userId, request.message(), request.page());
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(response.message()));
                emitter.send(SseEmitter.event()
                    .name("done")
                    .data("true"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/analyze-document")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AiService.AiResponse> analyzeDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "page", required = false) String page) {
        DocumentParser.ParsedDocument parsed = documentParser.parse(file);
        AiService.AiResponse response = aiService.analyzeDocument(getCurrentUserId(), parsed);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getSuggestions(
            @RequestParam(value = "page", required = false) String page) {
        String userId = getCurrentUserId();
        List<String> suggestions = aiService.getSuggestions(userId, page);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> clearHistory() {
        aiService.clearHistory(getCurrentUserId());
        return ResponseEntity.ok(Map.of("message", "Historique effacé"));
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
            "provider", "mimo",
            "model", "mimo-v2-pro",
            "available", true,
            "providers", List.of("mimo", "openai", "anthropic", "ollama", "openrouter")
        ));
    }

    private String getCurrentUserId() {
        try {
            return securityUtils.getCurrentUserId().toString();
        } catch (Exception e) {
            return "anonymous";
        }
    }

    public record ChatRequest(String message, String page) {}
}
