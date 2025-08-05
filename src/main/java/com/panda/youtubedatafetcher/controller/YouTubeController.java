package com.panda.youtubedatafetcher.controller;

import com.panda.youtubedatafetcher.service.YouTubeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    private final YouTubeService youTubeService;

    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @GetMapping("/search")
    public List<Map<String, String>> search(@RequestParam String query) throws Exception {
        return youTubeService.searchVideos(query);
    }
}
