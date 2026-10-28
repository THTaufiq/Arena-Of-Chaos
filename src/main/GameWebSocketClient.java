package main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

public class GameWebSocketClient {

    private WebSocket webSocket;

    private String getWebSocketHost() {
        String host = "localhost";
        try {
            java.io.File file = new java.io.File("websocket_config.txt");
            if (file.exists() && file.isFile()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String line = br.readLine();
                    if (line != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            host = line;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading websocket_config.txt: " + e.getMessage());
        }
        return host;
    }

    public void connect(java.util.function.Consumer<String> messageHandler) {
        String host = getWebSocketHost();
        String wsUrl = "ws://" + host + ":8080/ws/challenge";
        System.out.println("Connecting to WebSocket server: " + wsUrl);
        try {
            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocketListener(messageHandler))
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        System.out.println("Connected to WebSocket server.");
                    })
                    .exceptionally(ex -> {
                        System.err.println("Failed to connect to WebSocket (backend offline?): " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("WebSocket initialization error: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return webSocket != null;
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.sendText(message, true)
                    .exceptionally(ex -> {
                        System.err.println("Failed to send WebSocket message: " + ex.getMessage());
                        return null;
                    });
        } else {
            System.err.println("WebSocket is not connected. Cannot send message.");
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Goodbye")
                    .thenRun(() -> System.out.println("WebSocket closed."))
                    .exceptionally(ex -> {
                        System.err.println("Error closing WebSocket: " + ex.getMessage());
                        return null;
                    });
        }
    }

    private static class WebSocketListener implements Listener {
        private final StringBuilder accumulatedMessage = new StringBuilder();
        private final java.util.function.Consumer<String> messageHandler;

        public WebSocketListener(java.util.function.Consumer<String> messageHandler) {
            this.messageHandler = messageHandler;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("WebSocket connection opened locally.");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            accumulatedMessage.append(data);
            if (last) {
                String completeMessage = accumulatedMessage.toString();
                System.out.println("WebSocket Received: " + completeMessage);
                if (messageHandler != null) {
                    messageHandler.accept(completeMessage);
                }
                accumulatedMessage.setLength(0); // clear buffer
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("WebSocket connection closed by server. Status: " + statusCode + ", Reason: " + reason);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("WebSocket error occurred: " + error.getMessage());
            webSocket.request(1);
        }
    }
}
