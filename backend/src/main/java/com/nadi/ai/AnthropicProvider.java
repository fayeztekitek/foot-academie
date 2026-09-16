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
public class AnthropicProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicProvider.class);
    private final AiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient;

    public AnthropicProvider(AiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .defaultHeader("x-api-key", config.getApiKey())
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader("Content-Type", "application/json")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }

    @Override
    public String chat(String systemPrompt, String userMessage, List<Map<String, Object>> history) {
        try {
            ObjectNode body = buildBody(systemPrompt, userMessage, history);
            String response = webClient.post()
                .uri("/v1/messages")
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JsonNode json = mapper.readTree(response);
            JsonNode content = json.path("content");
            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText("");
            }
            return "";
        } catch (Exception e) {
            log.error("Anthropic chat error: {}", e.getMessage());
            throw new RuntimeException("Erreur IA: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithTools(String systemPrompt, String userMessage,
                                 List<Map<String, Object>> history,
                                 List<Map<String, Object>> tools) {
        try {
            ObjectNode body = buildBody(systemPrompt, userMessage, history);
            if (tools != null && !tools.isEmpty()) {
                ArrayNode toolsArr = body.putArray("tools");
                for (Map<String, Object> tool : tools) {
                    ObjectNode t = toolsArr.addObject();
                    t.put("name", (String) tool.get("name"));
                    t.put("description", (String) tool.get("description"));
                    if (tool.containsKey("parameters")) {
                        t.set("input_schema", mapper.valueToTree(tool.get("parameters")));
                    }
                }
            }
            String response = webClient.post()
                .uri("/v1/messages")
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JsonNode json = mapper.readTree(response);
            JsonNode content = json.path("content");
            if (content.isArray() && content.size() > 0) {
                JsonNode first = content.get(0);
                if ("tool_use".equals(first.path("type").asText())) {
                    return first.toString();
                }
                return first.path("text").asText("");
            }
            return "";
        } catch (Exception e) {
            log.error("Anthropic tools error: {}", e.getMessage());
            throw new RuntimeException("Erreur IA: " + e.getMessage(), e);
        }
    }

    private ObjectNode buildBody(String systemPrompt, String userMessage,
                                  List<Map<String, Object>> history) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", config.getModel());
        body.put("max_tokens", config.getMaxTokens());
        body.put("system", systemPrompt);

        ArrayNode messages = body.putArray("messages");
        if (history != null) {
            for (Map<String, Object> msg : history) {
                ObjectNode m = messages.addObject();
                m.put("role", "user".equals(msg.get("role")) ? "user" : "assistant");
                m.put("content", (String) msg.get("content"));
            }
        }
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", userMessage);
        return body;
    }

    @Override
    public String getProviderName() { return "anthropic"; }

    @Override
    public boolean isAvailable() {
        return config.getApiKey() != null && !config.getApiKey().isEmpty()
            && "anthropic".equals(config.getProvider());
    }
}
