package ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ScoreClient {

    private static final String HIGHEST_SCORE_URL = "http://localhost:8080/api/score/highest";
    private static final String SCORE_URL = "http://localhost:8080/api/score/save";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private ScoreClient() {
    }

    public static void saveScore(String playerName, int totalScore) {
        String safeName = sanitize(playerName);
        String payload = "{\"playerName\":\"" + safeName + "\",\"maxScore\":" + totalScore + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SCORE_URL))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        System.err.println("Score save failed (backend offline?): " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Score save setup failed: " + e.getMessage());
        }
    }

    public static int getHighScore() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HIGHEST_SCORE_URL))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseHighScore(response.body());
            }
        } catch (Exception e) {
            System.err.println("High score fetch failed (backend offline?): " + e.getMessage());
        }

        return 0;
    }

    private static int parseHighScore(String body) {
        if (body == null || body.isBlank()) {
            return 0;
        }

        String key = "\"highScore\"";
        int keyIndex = body.indexOf(key);
        if (keyIndex < 0) {
            return 0;
        }

        int colonIndex = body.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) {
            return 0;
        }

        int startIndex = colonIndex + 1;
        while (startIndex < body.length() && Character.isWhitespace(body.charAt(startIndex))) {
            startIndex++;
        }

        int endIndex = startIndex;
        while (endIndex < body.length() && Character.isDigit(body.charAt(endIndex))) {
            endIndex++;
        }

        if (startIndex == endIndex) {
            return 0;
        }

        try {
            return Integer.parseInt(body.substring(startIndex, endIndex));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "Player1";
        }

        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
