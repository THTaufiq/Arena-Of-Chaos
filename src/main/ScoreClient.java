package main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ScoreClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))
            .build();

    private static final String SCORE_URL = "http://localhost:8080/api/score/save";

    private ScoreClient() {
    }

    public static void sendGameOverScore(String playerName, int score) {
        String safeName = sanitizeName(playerName);
        String payload = "{\"playerName\":\"" + safeName + "\",\"score\":" + score + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SCORE_URL))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> null);
    }

    private static String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return "Player";
        }

        return name.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
