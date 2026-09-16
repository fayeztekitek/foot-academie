package com.nadi.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class OllamaProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaProvider.class);
    private final AiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient;

    public OllamaProvider(AiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
            .baseUrl(config.getOllamaUrl())
            .defaultHeader("Content-Type", "application/json")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }

    @Override
    public String chat(String systemPrompt, String userMessage, List<Map<String, Object>> history) {
        try {
            ObjectNode body = buildBody(systemPrompt, userMessage, history);
            String response = webClient.post()
                .uri("/api/chat")
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JsonNode json = mapper.readTree(response);
            return json.path("message").path("content").asText("");
        } catch (Exception e) {
            log.error("Ollama chat error: {}", e.getMessage());
            throw new RuntimeException("Erreur IA locale: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithTools(String systemPrompt, String userMessage,
                                 List<Map<String, Object>> history,
                                 List<Map<String, Object>> tools) {
        return chat(systemPrompt, userMessage, history);
    }

    private ObjectNode buildBody(String systemPrompt, String userMessage,
                                  List<Map<String, Object>> history) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", config.getModel());
        body.put("stream", false);

        ObjectNode options = body.putObject("options");
        options.put("temperature", config.getTemperature());
        options.put("num_predict", config.getMaxTokens());

        ArrayNode messages = body.putArray("messages");
        ObjectNode sys = messages.addObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);

        if (history != null) {
            for (Map<String, Object> msg : history) {
                ObjectNode m = messages.addObject();
                m.put("role", (String) msg.get("role"));
                m.put("content", (String) msg.get("content"));
            }
        }

        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", userMessage);
        return body;
    }

    @Override
    public String getProviderName() { return "ollama"; }

    @Override
    public boolean isAvailable() {
        try {
            webClient.get().uri("/api/tags").retrieve().bodyToMono(String.class).block();
            return "ollama".equals(config.getProvider());
        } catch (Exception e) {
            return false;
        }
    }
}
