package com.testarena.feedback.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testarena.feedback.dto.FeedbackResponse;
import org.springframework.stereotype.Component;

@Component
public class AiResponseParser {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Clean and parse raw model text into JsonNode (first object/array found)
     */
    public JsonNode parseAiText(String raw) throws Exception {
        if (raw == null) throw new IllegalArgumentException("raw is null");
        raw = raw.trim();
        // remove triple backticks and language e.g. ```json
        raw = raw.replaceAll("(?s)^```\\w*\\n", "");
        raw = raw.replaceAll("(?s)\\n```$", "");
        // find first JSON object
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");
        if (start == -1 || end == -1 || end <= start) {
            // try array
            int sa = raw.indexOf("[");
            int ea = raw.lastIndexOf("]");
            if (sa == -1 || ea == -1 || ea <= sa) {
                throw new IllegalStateException("No JSON found in AI text");
            }
            String arr = raw.substring(sa, ea + 1);
            return mapper.readTree(arr);
        } else {
            String json = raw.substring(start, end + 1);
            return mapper.readTree(json);
        }
    }

    public FeedbackResponse convertToFeedbackResponse(JsonNode node) throws Exception {
        return mapper.treeToValue(node, FeedbackResponse.class);
    }
}
