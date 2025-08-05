package com.panda.youtubedatafetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class YoutubeDataFetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoutubeDataFetcherApplication.class, args);
    }

}
