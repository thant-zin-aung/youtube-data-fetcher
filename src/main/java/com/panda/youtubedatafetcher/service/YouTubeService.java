package com.panda.youtubedatafetcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class YouTubeService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FirebaseService firebaseService;

    public YouTubeService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    public List<Map<String, String>> searchVideos(String query) throws Exception {
        String apiKey = firebaseService.getAvailableApiKey();
        if (apiKey == null) throw new RuntimeException("No API keys available!");

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet" +
                "&q=" + encodedQuery +
                "&type=video" +
                "&maxResults=50" +
                "&key=" + apiKey;

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                String reason = root.path("error").path("errors").get(0).path("reason").asText();
                if ("quotaExceeded".equals(reason)) {
                    firebaseService.markApiKeyAsLimited(apiKey);
                    return searchVideos(query); // retry with new key
                }
                throw new RuntimeException("YouTube API error: " + reason);
            }

            List<Map<String, String>> results = new ArrayList<>();
            JsonNode items = root.get("items");

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    JsonNode idNode = item.get("id");
                    JsonNode snippetNode = item.get("snippet");

                    if (idNode == null || idNode.get("videoId") == null || snippetNode == null) {
                        continue;
                    }

                    Map<String, String> videoData = new HashMap<>();
                    videoData.put("videoId", idNode.get("videoId").asText(""));
                    videoData.put("title", snippetNode.path("title").asText(""));
                    videoData.put("channelTitle", snippetNode.path("channelTitle").asText(""));
                    videoData.put("thumbnail", snippetNode.path("thumbnails").path("high").path("url").asText(""));

                    results.add(videoData);
                }
            }

            return results;

        } catch (Exception e) {
            // For HttpClientErrorException, check if quotaExceeded in body
            if (e.getMessage() != null && e.getMessage().contains("quotaExceeded")) {
                firebaseService.markApiKeyAsLimited(apiKey);
                return searchVideos(query); // retry with a new key
            }
            throw e;
        }
    }

}
