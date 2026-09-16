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
public class MimoProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(MimoProvider.class);
    private final AiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient;

    public MimoProvider(AiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
            .baseUrl(config.getBaseUrl())
            .defaultHeader("Authorization", "Bearer " + config.getApiKey())
            .defaultHeader("Content-Type", "application/json")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }

    @Override
    public String chat(String systemPrompt, String userMessage, List<Map<String, Object>> history) {
        try {
            ObjectNode body = buildRequestBody(systemPrompt, userMessage, history, null);
            String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode json = mapper.readTree(response);
            return json.path("choices").get(0).path("message").path("content").asText("");
        } catch (Exception e) {
            log.error("MIMO chat error: {}", e.getMessage());
            throw new RuntimeException("Erreur IA: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithTools(String systemPrompt, String userMessage,
                                 List<Map<String, Object>> history,
                                 List<Map<String, Object>> tools) {
        try {
            ObjectNode body = buildRequestBody(systemPrompt, userMessage, history, tools);
            String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode json = mapper.readTree(response);
            JsonNode message = json.path("choices").get(0).path("message");

            if (message.has("tool_calls") && !message.get("tool_calls").isNull()) {
                return message.get("tool_calls").toString();
            }
            return message.path("content").asText("");
        } catch (Exception e) {
            log.error("MIMO tools error: {}", e.getMessage());
            throw new RuntimeException("Erreur IA: " + e.getMessage(), e);
        }
    }

    private ObjectNode buildRequestBody(String systemPrompt, String userMessage,
                                         List<Map<String, Object>> history,
                                         List<Map<String, Object>> tools) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", config.getModel());
        body.put("max_completion_tokens", config.getMaxTokens());
        body.put("temperature", config.getTemperature());

        ArrayNode messages = body.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        if (history != null) {
            for (Map<String, Object> msg : history) {
                ObjectNode histMsg = messages.addObject();
                histMsg.put("role", (String) msg.get("role"));
                histMsg.put("content", (String) msg.get("content"));
            }
        }

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        if (tools != null && !tools.isEmpty()) {
            ArrayNode toolsArray = body.putArray("tools");
            for (Map<String, Object> tool : tools) {
                ObjectNode toolNode = toolsArray.addObject();
                toolNode.put("type", "function");
                ObjectNode function = toolNode.putObject("function");
                function.put("name", (String) tool.get("name"));
                function.put("description", (String) tool.get("description"));
                if (tool.containsKey("parameters")) {
                    function.set("parameters", mapper.valueToTree(tool.get("parameters")));
                }
            }
            body.put("tool_choice", "auto");
        }

        return body;
    }

    @Override
    public String getProviderName() {
        return "mimo";
    }

    @Override
    public boolean isAvailable() {
        return config.getApiKey() != null && !config.getApiKey().isEmpty();
    }
}
