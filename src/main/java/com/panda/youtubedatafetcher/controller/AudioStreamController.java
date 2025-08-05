package com.panda.youtubedatafetcher.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
public class AudioStreamController {

    @GetMapping("/stream-audio")
    public void streamAudio(
            @RequestParam String url,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // Step 1: Get direct audio URL
        String directUrl = getDirectAudioUrl(url);
        if (directUrl == null) {
            response.sendError(500, "Could not get direct audio URL");
            return;
        }

        // Step 2: Forward Range header if client requests it
        String range = request.getHeader("Range");

        HttpURLConnection conn = (HttpURLConnection) new URL(directUrl).openConnection();
        if (range != null) {
            conn.setRequestProperty("Range", range);
        }

        // Step 3: Copy headers
        String contentType = conn.getContentType();
        int contentLength = conn.getContentLength();
        int responseCode = conn.getResponseCode();

        response.setContentType(contentType);
        if (responseCode == 206) { // Partial Content
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }
        if (contentLength > 0) {
            response.setHeader("Content-Length", String.valueOf(contentLength));
        }
        String contentRange = conn.getHeaderField("Content-Range");
        if (contentRange != null) {
            response.setHeader("Content-Range", contentRange);
        }
        response.setHeader("Accept-Ranges", "bytes");

        // Step 4: Stream data
        try (var in = conn.getInputStream(); var out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }

    private String getDirectAudioUrl(String youtubeUrl) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-f", "bestaudio", "-g", youtubeUrl);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.readLine();
        }
    }
}
