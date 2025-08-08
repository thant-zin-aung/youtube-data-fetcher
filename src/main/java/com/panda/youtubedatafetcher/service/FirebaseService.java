package com.panda.youtubedatafetcher.service;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class FirebaseService {

    private Firestore db;

    // for development
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

    // for production
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
        return getAvailableApiKeyWithRetry(0);
    }

    private String getAvailableApiKeyWithRetry(int attempt) throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("youtube_api_keys")
                .whereEqualTo("isLimitReach", false)
                .limit(1)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        if (!documents.isEmpty()) {
            return documents.get(0).getString("apiKey");
        }

        if (attempt < 3) {
            System.out.println("[FirebaseService] No available API keys. Resetting all (Attempt " + (attempt + 1) + ")...");
            resetAllApiKeys();
            return getAvailableApiKeyWithRetry(attempt + 1);
        } else {
            throw new RuntimeException("No available YouTube API keys after 3 reset attempts.");
        }
    }

    private void resetAllApiKeys() throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("youtube_api_keys").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            db.collection("youtube_api_keys")
                    .document(doc.getId())
                    .update("isLimitReach", false);
        }

        System.out.println("[FirebaseService] All API keys reset to false.");
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
}
