package com.testarena.feedback.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testarena.feedback.dto.SearchResultItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleSearchClient {
    private final WebClient webClient;
    private final String apiKey;
    private final String cx;
    private final ObjectMapper mapper = new ObjectMapper();

    public GoogleSearchClient(@Value("${google.cse.url}") String baseUrl,
                              @Value("${google.api.key}") String apiKey,
                              @Value("${google.cse.cx}") String cx) {
        this.apiKey = apiKey;
        this.cx = cx;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public List<SearchResultItem> search(String query, int num) {
        try {
            String url = UriComponentsBuilder.fromUriString("")
                    .queryParam("key", apiKey)
                    .queryParam("cx", cx)
                    .queryParam("q", query)
                    .queryParam("num", num)
                    .build()
                    .toUriString();

            String resp = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = mapper.readTree(resp);
            List<SearchResultItem> items = new ArrayList<>();
            JsonNode arr = root.path("items");
            if (arr.isArray()) {
                for (JsonNode it : arr) {
                    String title = it.path("title").asText();
                    String snippet = it.path("snippet").asText();
                    String link = it.path("link").asText();
                    items.add(new SearchResultItem(title, snippet, link));
                }
            }
            return items;
        } catch (Exception e) {
            throw new RuntimeException("Google CSE search failed: " + e.getMessage(), e);
        }
    }
}
