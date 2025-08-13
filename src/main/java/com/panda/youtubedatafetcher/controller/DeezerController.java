package com.panda.youtubedatafetcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panda.youtubedatafetcher.service.DeezerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deezer")
@CrossOrigin(origins = "*")
public class DeezerController {

    private final DeezerService deezerService;
    private final ObjectMapper objectMapper;

    public DeezerController(DeezerService deezerService) {
        this.deezerService = deezerService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/search/tracks")
    public Object searchTracks(@RequestParam String q, @RequestParam(defaultValue = "10") int limit) {
        return deezerService.searchTracks(q, limit);
    }

    @GetMapping("/tracks/top")
    public ResponseEntity<Object> getTopTracks() {
        try {
            String json = deezerService.getTopTracks(5); // get top 5
            // Convert JSON string to Map so Spring returns it as JSON
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching top tracks");
        }
    }


    @GetMapping("/artists/top")
    public Object getTopArtists() {
        return deezerService.getTopArtists();
    }

    @GetMapping("/artists/{artistId}/albums")
    public Object getAlbumsByArtist(@PathVariable Long artistId) {
        return deezerService.getAlbumsByArtist(artistId);
    }

    @GetMapping("/albums/{albumId}/tracks")
    public Object getTracksByAlbum(@PathVariable Long albumId) {
        return deezerService.getTracksByAlbum(albumId);
    }
}
