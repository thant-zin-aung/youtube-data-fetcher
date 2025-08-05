package com.panda.youtubedatafetcher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uptime")
public class UptimeController {
    @GetMapping("/check")
    public ResponseEntity<Object> check() {
        return ResponseEntity.ok("Server is up and running...");
    }
}
