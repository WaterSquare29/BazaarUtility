package com.watersquare.bazaarutility;
//webhook https://ptb.discord.com/api/webhooks/1410098604928139325/Q7P9M9kiZlcMy_i7HAvHI9T0zb5xsIm_GHOb-k3-1K20DxaZlY2XKPbEHHpWzte9zopZ

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordNotifier {

    private static final String WEBHOOK_URL = "https://ptb.discord.com/api/webhooks/1410098604928139325/Q7P9M9kiZlcMy_i7HAvHI9T0zb5xsIm_GHOb-k3-1K20DxaZlY2XKPbEHHpWzte9zopZ";

    public static void sendToDiscord(String message) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Escape quotes for JSON
            message = message.replace("\"", "\\\"");

            String payload = "{\"content\": \"" + message + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes("utf-8"));
            }

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                System.out.println("Discord webhook failed: " + code);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}