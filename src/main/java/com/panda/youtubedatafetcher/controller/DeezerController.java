package com.panda.youtubedatafetcher.controller;

import com.panda.youtubedatafetcher.service.DeezerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deezer")
@CrossOrigin(origins = "*")
public class DeezerController {

    private final DeezerService deezerService;

    public DeezerController(DeezerService deezerService) {
        this.deezerService = deezerService;
    }

    @GetMapping("/search/tracks")
    public Object searchTracks(@RequestParam String q, @RequestParam(defaultValue = "10") int limit) {
        return deezerService.searchTracks(q, limit);
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
