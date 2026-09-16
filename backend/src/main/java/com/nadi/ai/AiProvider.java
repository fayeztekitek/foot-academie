package com.nadi.ai;

import java.util.List;
import java.util.Map;

public interface AiProvider {

    String chat(String systemPrompt, String userMessage, List<Map<String, Object>> history);

    String chatWithTools(String systemPrompt, String userMessage, List<Map<String, Object>> history,
                         List<Map<String, Object>> tools);

    default String chatWithToolResult(String systemPrompt, String userMessage,
                                       List<Map<String, Object>> history,
                                       List<Map<String, Object>> tools,
                                       Map<String, Object> toolResult) {
        return chatWithTools(systemPrompt, userMessage, history, tools);
    }

    String getProviderName();

    boolean isAvailable();
}
