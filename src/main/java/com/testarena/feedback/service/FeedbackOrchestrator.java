package com.testarena.feedback.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.testarena.feedback.client.GeminiClient;
import com.testarena.feedback.client.GoogleSearchClient;
import com.testarena.feedback.dto.FeedbackResponse;
import com.testarena.feedback.dto.SearchResultItem;
import com.testarena.feedback.util.AiResponseParser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackOrchestrator {
    private final GeminiClient gemini;
    private final GoogleSearchClient searchClient;
    private final AiResponseParser parser;

    public FeedbackOrchestrator(GeminiClient gemini,
                                GoogleSearchClient searchClient,
                                AiResponseParser parser) {
        this.gemini = gemini;
        this.searchClient = searchClient;
        this.parser = parser;
    }

    /**
     * Top-level method: returns the FeedbackResponse (topics array)
     */
    public FeedbackResponse provideFeedbackFor(String testId) {
        // 1) Load test summary from DB or mock (for demo we create a small mock)
        // You should replace this with your real test retrieval.
        String studentSummary = "{\"testId\":\"" + testId + "\",\"score\":62,\"weakAreas\":[\"Networking\"]}";

        // 2) Ask Gemini to produce a search query for weaknesses
        String geminiQueryText = gemini.generateQueryFor(studentSummary);

        // attempt to parse JSON query json like {"query":"dns tutorial for beginners"}
        String query = gemini.extractQueryFromRaw(geminiQueryText);

        // 3) Call Google CSE with the query
        List<SearchResultItem> results = searchClient.search(query, 5);

        // 4) Call Gemini summarizer to produce topics JSON
        String summarizerRaw = gemini.summarizeWithSearchResults(studentSummary, results);

        // 5) Parse AI response to JSON and map to DTO
        try {
            JsonNode node = parser.parseAiText(summarizerRaw);
            // convert node into FeedbackResponse using Jackson
            return parser.convertToFeedbackResponse(node);
        } catch (Exception e) {
            // fallback: return empty response with an error message
            FeedbackResponse fallback = new FeedbackResponse();
            fallback.setTopics(List.of());
            fallback.setMessage("Failed to parse AI response: " + e.getMessage());
            return fallback;
        }
    }
}
