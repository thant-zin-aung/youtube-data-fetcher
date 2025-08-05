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
            List<Map<String, String>> results = new ArrayList<>();

            for (JsonNode item : root.get("items")) {
                Map<String, String> videoData = new HashMap<>();
                videoData.put("videoId", item.get("id").get("videoId").asText());
                videoData.put("title", item.get("snippet").get("title").asText());
                videoData.put("channelTitle", item.get("snippet").get("channelTitle").asText());
                videoData.put("thumbnail", item.get("snippet").get("thumbnails").get("high").get("url").asText());
                results.add(videoData);
            }
            return results;

        } catch (Exception e) {
            if (e.getMessage().contains("quotaExceeded")) {
                firebaseService.markApiKeyAsLimited(apiKey);
                // Retry with a new key
                return searchVideos(query);
            }
            throw e;
        }
    }
}
