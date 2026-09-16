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
public class OpenAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);
    private final AiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient;

    public OpenAiProvider(AiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader("Authorization", "Bearer " + config.getApiKey())
            .defaultHeader("Content-Type", "application/json")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }

    @Override
    public String chat(String systemPrompt, String userMessage, List<Map<String, Object>> history) {
        try {
            ObjectNode body = buildBody(systemPrompt, userMessage, history, null);
            String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JsonNode json = mapper.readTree(response);
            return json.path("choices").get(0).path("message").path("content").asText("");
        } catch (Exception e) {
            log.error("OpenAI chat error: {}", e.getMessage());
            throw new RuntimeException("Erreur IA: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithTools(String systemPrompt, String userMessage,
                                 List<Map<String, Object>> history,
                                 List<Map<String, Object>> tools) {
        try {
            ObjectNode body = buildBody(systemPrompt, userMessage, history, tools);
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
            log.error("OpenAI tools error: {}", e.getMessage());
            throw new RuntimeException("Erreur IA: " + e.getMessage(), e);
        }
    }

    private ObjectNode buildBody(String systemPrompt, String userMessage,
                                  List<Map<String, Object>> history,
                                  List<Map<String, Object>> tools) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", config.getModel());
        body.put("max_tokens", config.getMaxTokens());
        body.put("temperature", config.getTemperature());

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

        if (tools != null && !tools.isEmpty()) {
            ArrayNode toolsArr = body.putArray("tools");
            for (Map<String, Object> tool : tools) {
                ObjectNode t = toolsArr.addObject();
                t.put("type", "function");
                ObjectNode fn = t.putObject("function");
                fn.put("name", (String) tool.get("name"));
                fn.put("description", (String) tool.get("description"));
                if (tool.containsKey("parameters")) {
                    fn.set("parameters", mapper.valueToTree(tool.get("parameters")));
                }
            }
            body.put("tool_choice", "auto");
        }
        return body;
    }

    @Override
    public String getProviderName() { return "openai"; }

    @Override
    public boolean isAvailable() {
        return config.getApiKey() != null && !config.getApiKey().isEmpty()
            && "openai".equals(config.getProvider());
    }
}
