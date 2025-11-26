package com.testarena.feedback.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testarena.feedback.dto.SearchResultItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeminiClient {
    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int maxOutputTokens;
    private final double temperature;

    public GeminiClient(@Value("${gemini.api.url}") String baseUrl,
                        @Value("${gemini.api.key}") String apiKey,
                        @Value("${gemini.maxOutputTokens:700}") int maxOutputTokens,
                        @Value("${gemini.temperature:0.0}") double temperature) {
        this.apiKey = apiKey;
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    /**
     * Ask Gemini to generate a short search query JSON for the student's weak areas.
     * Returns the raw text from the model (may include fences)
     */
    public String generateQueryFor(String studentSummaryJson) {
        try {
            String prompt = "Task: Return EXACTLY one JSON object with single field 'query' to search " +
                    "for helpful learning resources for the student's weaknesses.\n" +
                    "StudentSummary: " + studentSummaryJson + "\n" +
                    "Example output: {\"query\":\"dns tutorial for beginners\"}\n" +
                    "Return ONLY the JSON, no markdown, no fences.";

            // build body using ObjectNode / ArrayNode
            ObjectNode body = mapper.createObjectNode();

            ArrayNode contents = mapper.createArrayNode();
            ObjectNode contentEntry = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();
            ObjectNode part = mapper.createObjectNode().put("text", prompt);
            parts.add(part);
            contentEntry.set("parts", parts);
            contents.add(contentEntry);

            ObjectNode config = mapper.createObjectNode();
            config.put("temperature", temperature);
            config.put("maxOutputTokens", maxOutputTokens);

            body.set("contents", contents);
            body.set("config", config);

            String resp = webClient.post()
                    .bodyValue(mapper.writeValueAsString(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (resp == null || resp.isBlank()) {
                throw new RuntimeException("Empty response from Gemini");
            }

            // The response is an entire JSON; we need to extract model text.
            JsonNode root = mapper.readTree(resp);
            JsonNode candidates = root.path("candidates");
            JsonNode firstCandidate = null;

            if (candidates.isArray() && candidates.size() > 0) {
                firstCandidate = candidates.get(0);
            } else if (!candidates.isMissingNode() && candidates.isObject()) {
                firstCandidate = candidates;
            } else {
                // fallback: maybe older response shape
                firstCandidate = root.path("candidate");
            }

            if (firstCandidate == null || firstCandidate.isMissingNode()) {
                // Last resort: return raw response
                return resp;
            }

            JsonNode contentParts = firstCandidate.path("content").path("parts");
            if (contentParts.isArray() && contentParts.size() > 0) {
                return contentParts.get(0).path("text").asText();
            }

            // sometimes content is directly under "text"
            JsonNode textNode = firstCandidate.path("text");
            if (!textNode.isMissingNode()) return textNode.asText();

            return firstCandidate.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini: " + e.getMessage(), e);
        }
    }


    public String extractQueryFromRaw(String raw) {
        try {
            if (raw == null) return "";
            // simple: take first {..} block and parse
            int start = raw.indexOf("{");
            int end = raw.lastIndexOf("}");
            if (start == -1 || end == -1 || end <= start) return raw.trim();
            String json = raw.substring(start, end + 1);
            JsonNode node = mapper.readTree(json);
            if (node.has("query")) return node.get("query").asText();
            return json;
        } catch (Exception e) {
            // return the raw text if parsing fails
            return raw;
        }
    }

    public String summarizeWithSearchResults(String studentSummaryJson, List<SearchResultItem> results) {
        try {
            // build search results JSON array
            ArrayNode resultArray = mapper.createArrayNode();
            for (SearchResultItem r : results) {
                ObjectNode obj = mapper.createObjectNode();
                // expects getters on SearchResultItem
                obj.put("title", r.getTitle());
                obj.put("snippet", r.getSnippet());
                obj.put("link", r.getLink());
                resultArray.add(obj);
            }
            String searchJson = mapper.writeValueAsString(resultArray);

            String prompt = "Repository snapshot: file:///mnt/data/CurrentProg.txt\n" +
                    "StudentSummary: " + studentSummaryJson + "\n" +
                    "SearchResults: " + searchJson + "\n\n" +
                    "Task: Using the above data, produce EXACTLY one JSON object, no markdown, no backticks, no commentary, with this shape:\n" +
                    "{ \"topics\": [ { \"topicName\": \"...\", \"feedback\": \"...\", \"recommendedLinks\": [\"https://...\", ...] } ] }\n" +
                    "Rules: keep feedback to 1-2 short sentences; use at most 3 recommendedLinks per topic; prefer canonical/official resources.";

            // build body
            ObjectNode body = mapper.createObjectNode();

            ArrayNode contents = mapper.createArrayNode();
            ObjectNode contentEntry = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();
            ObjectNode part = mapper.createObjectNode().put("text", prompt);
            parts.add(part);
            contentEntry.set("parts", parts);
            contents.add(contentEntry);

            ObjectNode config = mapper.createObjectNode();
            config.put("temperature", temperature);
            config.put("maxOutputTokens", maxOutputTokens);

            body.set("contents", contents);
            body.set("config", config);

            String resp = webClient.post()
                    .bodyValue(mapper.writeValueAsString(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (resp == null || resp.isBlank()) {
                throw new RuntimeException("Empty response from Gemini summarizer");
            }

            // extract candidate text like above
            JsonNode root = mapper.readTree(resp);
            JsonNode candidates = root.path("candidates");
            JsonNode firstCandidate = null;

            if (candidates.isArray() && candidates.size() > 0) {
                firstCandidate = candidates.get(0);
            } else if (!candidates.isMissingNode() && candidates.isObject()) {
                firstCandidate = candidates;
            } else {
                firstCandidate = root.path("candidate");
            }

            if (firstCandidate == null || firstCandidate.isMissingNode()) {
                return resp;
            }

            JsonNode partsNode = firstCandidate.path("content").path("parts");
            if (partsNode.isArray() && partsNode.size() > 0) {
                return partsNode.get(0).path("text").asText();
            }

            JsonNode textNode = firstCandidate.path("text");
            if (!textNode.isMissingNode()) return textNode.asText();

            return firstCandidate.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini summarizer: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to produce a compact student summary JSON from whatever internal object you have.
     * (Optional, example only)
     */
    public String makeStudentSummaryJson(String userId, String testId, String shortNote) {
        try {
            ObjectNode o = mapper.createObjectNode();
            o.put("userId", userId);
            o.put("testId", testId);
            o.put("note", shortNote);
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}
