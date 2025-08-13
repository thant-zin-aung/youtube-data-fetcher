package com.panda.youtubedatafetcher.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Service
public class DeezerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://api.deezer.com";

    public Object searchTracks(String query, int limit) {
        String encoded = UriUtils.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search?q=" + encoded + "&limit=" + limit;
        return restTemplate.getForObject(url, Object.class);
    }

    public String getTopTracks(int limit) {
        String url = "https://api.deezer.com/chart/0/tracks?limit=" + limit;
        return restTemplate.getForObject(url, String.class);
    }


    public Object getTopArtists() {
        String url = BASE_URL + "/chart/0/artists?limit=100";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getAlbumsByArtist(Long artistId) {
        String url = BASE_URL + "/artist/" + artistId + "/albums";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getTracksByAlbum(Long albumId) {
        String url = BASE_URL + "/album/" + albumId + "/tracks";
        return restTemplate.getForObject(url, Object.class);
    }
}
