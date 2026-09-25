package com.nadi.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiConfig config;
    private final PromptTemplates promptTemplates;
    private final AiContextBuilder contextBuilder;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final DocumentParser documentParser;
    private final Map<String, AiProvider> providers;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, List<Map<String, Object>>> conversationHistory = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> rateLimiter = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ai-rate-limiter");
        t.setDaemon(true);
        return t;
    });

    public AiService(AiConfig config, PromptTemplates promptTemplates,
                     AiContextBuilder contextBuilder, ToolRegistry toolRegistry,
                     ToolExecutor toolExecutor, DocumentParser documentParser,
                     List<AiProvider> providerList) {
        this.config = config;
        this.promptTemplates = promptTemplates;
        this.contextBuilder = contextBuilder;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.documentParser = documentParser;
        this.providers = new ConcurrentHashMap<>();
        for (AiProvider p : providerList) {
            providers.put(p.getProviderName(), p);
        }
    }

    public AiResponse chat(String userId, String message, String page) {
        if (isRateLimited(userId)) {
            return new AiResponse("Trop de requêtes. Veuillez patienter une minute.", false, null);
        }

        try {
            AiProvider provider = getActiveProvider();
            String role = getUserRole(userId);
            String systemPrompt = promptTemplates.getSystemPrompt(role, page);
            String context = contextBuilder.buildContextString(page);

            List<Map<String, Object>> history = conversationHistory
                .computeIfAbsent(userId, k -> new ArrayList<>());

            String fullPrompt = systemPrompt + "\n\n" + context;
            List<Map<String, Object>> tools = toolRegistry.getToolsForRole(role);

            String response;
            if (tools != null && tools.size() > 3) {
                List<Map<String, Object>> limitedTools = tools.subList(0, 3);
                response = provider.chatWithTools(fullPrompt, message, history, limitedTools);
            } else {
                response = provider.chatWithTools(fullPrompt, message, history, tools);
            }

            if (isToolCall(response)) {
                return handleToolCall(userId, response, message, fullPrompt, history, role);
            }

            history.add(Map.of("role", "user", "content", message));
            history.add(Map.of("role", "assistant", "content", response));

            trimHistory(history, 20);

            return new AiResponse(response, true, null);
        } catch (Exception e) {
            log.error("AI chat error for user {}: {}", userId, e.getMessage());
            return new AiResponse("Désolé, une erreur est survenue: " + e.getMessage(), false, null);
        }
    }

    public AiResponse analyzeDocument(String userId, DocumentParser.ParsedDocument doc) {
        try {
            AiProvider provider = getActiveProvider();
            String role = getUserRole(userId);
            String systemPrompt = promptTemplates.getDocumentAnalysisPrompt(doc.type());
            String message = "Analyse ce document:\n\nType: " + doc.type() +
                "\nMétadonnées: " + doc.metadata() +
                "\n\nContenu:\n" + doc.content().substring(0, Math.min(4000, doc.content().length()));

            String response = provider.chat(systemPrompt, message, null);
            return new AiResponse(response, true, doc.metadata());
        } catch (Exception e) {
            return new AiResponse("Erreur d'analyse: " + e.getMessage(), false, null);
        }
    }

    public List<String> getSuggestions(String userId, String page) {
        try {
            AiProvider provider = getActiveProvider();
            String role = getUserRole(userId);
            String prompt = promptTemplates.getSuggestionsPrompt(role, page);
            String response = provider.chat("Tu es un assistant JSON.", prompt, null);

            JsonNode json = mapper.readTree(response);
            JsonNode suggestions = json.path("suggestions");
            if (suggestions.isArray()) {
                List<String> result = new ArrayList<>();
                suggestions.forEach(s -> result.add(s.asText()));
                return result;
            }
            return List.of("Résumé du tableau de bord", "Joueurs en retard de paiement", "Planning de la semaine");
        } catch (Exception e) {
            return List.of("Résumé du tableau de bord", "Joueurs en retard de paiement", "Planning de la semaine");
        }
    }

    public void clearHistory(String userId) {
        conversationHistory.remove(userId);
    }

    private AiResponse handleToolCall(String userId, String toolCallJson, String originalMessage,
                                       String systemPrompt, List<Map<String, Object>> history, String role) {
        try {
            JsonNode toolCalls = mapper.readTree(toolCallJson);
            StringBuilder results = new StringBuilder();

            for (JsonNode tc : toolCalls) {
                String toolName = tc.path("function").path("name").asText();
                String argsStr = tc.path("function").path("arguments").asText("{}");
                Map<String, Object> args = mapper.readValue(argsStr, new TypeReference<>() {});
                String toolResult = toolExecutor.execute(toolName, args);
                if (toolResult.length() > 2000) {
                    toolResult = toolResult.substring(0, 2000) + "... (données tronquées)";
                }
                results.append("Résultat de ").append(toolName).append(": ").append(toolResult).append("\n");
            }

            AiProvider provider = getActiveProvider();
            String followUpPrompt = systemPrompt +
                "\n\nVoici les données obtenues:\n" + results +
                "\n\nRéponds à l'utilisateur en te basant sur ces données. Sois concis et utile.";
            String finalResponse = provider.chat(followUpPrompt, originalMessage, history);

            history.add(Map.of("role", "user", "content", originalMessage));
            history.add(Map.of("role", "assistant", "content", finalResponse));
            trimHistory(history, 20);

            return new AiResponse(finalResponse, true, null);
        } catch (Exception e) {
            return new AiResponse("Erreur lors de l'exécution de l'action: " + e.getMessage(), false, null);
        }
    }

    private boolean isToolCall(String response) {
        if (response == null) return false;
        try {
            JsonNode json = mapper.readTree(response);
            return json.isArray() && json.size() > 0 && json.get(0).has("function");
        } catch (Exception e) {
            return false;
        }
    }

    private AiProvider getActiveProvider() {
        String providerName = config.getProvider();
        AiProvider provider = providers.get(providerName);
        if (provider == null || !provider.isAvailable()) {
            provider = providers.values().stream()
                .filter(AiProvider::isAvailable)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun provider IA disponible"));
        }
        return provider;
    }

    private String getUserRole(String userId) {
        try {
            // Least-privilege fallback: if the security context is unavailable
            // (e.g. background thread), never escalate to ADMIN — restrict to
            // PARENT-level tools instead of failing open.
            return contextBuilder.buildContext("chat").getOrDefault("userRole", "PARENT").toString();
        } catch (Exception e) {
            return "PARENT";
        }
    }

    private boolean isRateLimited(String userId) {
        // Never share buckets across users: failures resolving the user get a
        // per-request unique key instead of one global "anonymous" bucket.
        String bucket = (userId == null || userId.isBlank()) ? "anon-" + UUID.randomUUID() : userId;
        AtomicInteger count = rateLimiter.computeIfAbsent(bucket, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > config.getRateLimitPerMinute()) {
            return true;
        }
        if (count.get() == 1) {
            scheduler.schedule(() -> rateLimiter.remove(bucket), 60, TimeUnit.SECONDS);
        }
        return false;
    }

    private void trimHistory(List<Map<String, Object>> history, int maxSize) {
        while (history.size() > maxSize) {
            history.remove(0);
            if (!history.isEmpty()) history.remove(0);
        }
    }

    public record AiResponse(String message, boolean success, Map<String, Object> metadata) {}
}
