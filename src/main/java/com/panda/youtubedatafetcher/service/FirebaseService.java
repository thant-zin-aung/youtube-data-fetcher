package com.panda.youtubedatafetcher.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.google.api.core.ApiFuture;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class FirebaseService {

    private Firestore db;

    @PostConstruct
    public void init() throws Exception {
        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/firebase-service-account.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
        db = FirestoreClient.getFirestore();
    }

//    @PostConstruct
//    public void init() throws Exception {
//        String firebaseJson = System.getenv("FIREBASE_CREDENTIALS");
//        if (firebaseJson == null || firebaseJson.isEmpty()) {
//            throw new RuntimeException("FIREBASE_CREDENTIALS environment variable is not set!");
//        }
//
//        InputStream serviceAccount =
//                new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));
//
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//
//        if (FirebaseApp.getApps().isEmpty()) {
//            FirebaseApp.initializeApp(options);
//        }
//        db = FirestoreClient.getFirestore();
//    }

    public String getAvailableApiKey() throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("youtube_api_keys")
                .whereEqualTo("isLimitReach", false)
                .limit(1)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) return null;

        return documents.get(0).getString("apiKey");
    }

    public void markApiKeyAsLimited(String apiKey) throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("youtube_api_keys")
                .whereEqualTo("apiKey", apiKey)
                .limit(1)
                .get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        if (!docs.isEmpty()) {
            String docId = docs.get(0).getId();
            db.collection("youtube_api_keys").document(docId)
                    .update("isLimitReach", true);
        }
    }

    // inside FirebaseService class
    @Scheduled(cron = "0 0 0 * * *") // every day at midnight server time
    public void resetApiKeyLimits() throws Exception {
        var future = db.collection("youtube_api_keys").get();
        var docs = future.get().getDocuments();

        for (var doc : docs) {
            if (Boolean.TRUE.equals(doc.getBoolean("isLimitReach"))) {
                db.collection("youtube_api_keys").document(doc.getId())
                        .update("isLimitReach", false);
            }
        }
        System.out.println("[Scheduler] Reset all API keys to isLimitReach=false");
    }
}
