package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.Scanner;


public class DropboxAPIClient {

    private static final String CLIENT_ID = "pk41yq6d8083rkb";
    private static final String CLIENT_SECRET = "ltjfnd8fmphkjij";
    private static final String REDIRECT_URI = "https://oauth.pstmn.io/v1/callback";

    private String accessToken;
    private String refreshToken;
    private final HttpClient httpClient;

    public DropboxAPIClient() {
        this.httpClient = HttpClient.newBuilder().build();
    }

    public String generateAuthorizationUrl() {
        return "https://www.dropbox.com/oauth2/authorize" +
                "?client_id=" + CLIENT_ID +
                "&response_type=code" +
                "&redirect_uri=" + REDIRECT_URI +
                "&token_access_type=offline" +
                "&scope=account_info.read";
    }

    public boolean exchangeCodeForTokens(String authorizationCode) {
        try {
            if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
                System.out.println("Invalid authorization code");
                return false;
            }

            String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
            String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            String formData = "code=" + authorizationCode +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=" + REDIRECT_URI;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.dropboxapi.com/oauth2/token"))
                    .header("Authorization", "Basic " + base64Credentials)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            System.out.println("Token exchange status: " + response.statusCode());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Token response: " + responseBody);

                this.accessToken = extractJsonValue(responseBody, "access_token");
                this.refreshToken = extractJsonValue(responseBody, "refresh_token");

                System.out.println("Token exchange successful!");
                System.out.println("Access Token: " + (accessToken != null ? accessToken.substring(0, 20) + "..." : "NULL"));
                System.out.println("Refresh Token: " + (refreshToken != null ? refreshToken.substring(0, 20) + "..." : "NULL"));

                // DEBUG: Check if tokens were actually extracted
                if (accessToken == null) {
                    System.out.println("WARNING: Access token extraction failed!");
                    System.out.println("Manual extraction from response:");
                    int start = responseBody.indexOf("\"access_token\":\"") + 16;
                    int end = responseBody.indexOf("\"", start);
                    if (start > 15 && end > start) {
                        this.accessToken = responseBody.substring(start, end);
                        System.out.println("Manually extracted token: " + accessToken.substring(0, 20) + "...");
                    }
                }
                return true;
            } else {
                System.out.println("Token exchange failed: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.out.println("Exception during token exchange: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void getCurrentAccountInfo() {
        try {
            System.out.println("\n=== Getting Current Account Info ===");

            if (accessToken == null) {
                System.out.println("No access token available");
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.dropboxapi.com/2/users/get_current_account"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString("null"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            System.out.println("Account API status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("Success!");
                String responseBody = response.body();
                System.out.println("Account Info: " + responseBody);
            } else {
                System.out.println("Failed: " + response.statusCode());
                System.out.println("Response: " + response.body());

                if (response.statusCode() == 401) {
                    System.out.println("💡 Token might be invalid or expired");
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting account info: " + e.getMessage());
        }
    }

    public void getTeamInfo() {
        try {
            System.out.println("=== Trying to Get Team/Organization Name ===");

            if (accessToken == null) {
                System.out.println("No access token available");
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.dropboxapi.com/2/team/get_info"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString("null"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            System.out.println("Team API status: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("Success!");
                String responseBody = response.body();
                System.out.println("Team Info: " + responseBody);
            } else {
                System.out.println("Team API Failed: " + response.statusCode());
                System.out.println("Response: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("Error getting team info: " + e.getMessage());
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) {
                // Try without quotes for values
                searchKey = "\"" + key + "\":";
                keyIndex = json.indexOf(searchKey);
                if (keyIndex == -1) return null;

                int valueStart = keyIndex + searchKey.length();
                int valueEnd = json.indexOf(",", valueStart);

                if (valueEnd == -1) valueEnd = json.indexOf("}", valueStart);
                if (valueEnd == -1) return null;

                String value = json.substring(valueStart, valueEnd).trim();
                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }

            int valueStart = keyIndex + searchKey.length();
            int valueEnd = json.indexOf("\"", valueStart);
            if (valueEnd == -1) return null;

            return json.substring(valueStart, valueEnd);
        } catch (Exception e) {
            System.out.println("Error extracting JSON value for key: " + key);
            return null;
        }
    }

    public static void main(String[] args) {
        DropboxAPIClient client = new DropboxAPIClient();
        Scanner scanner = new Scanner(System.in);

        try {
            String authUrl = client.generateAuthorizationUrl();
            System.out.println("Authorization URL:");
            System.out.println(authUrl);

            System.out.println("Instructions:");
            System.out.println("1. Open this URL in your browser");
            System.out.println("2. Login with your Dropbox account");
            System.out.println("3. Approve the permissions");
            System.out.println("4. Copy the ENTIRE redirect URL and paste below");

            System.out.print("Paste the redirect URL: ");
            String redirectUrl = scanner.nextLine().trim();

            String authCode = cleanAuthorizationCode(redirectUrl);
            System.out.println("Extracted code: " + authCode.substring(0, Math.min(20, authCode.length())) + "...");

            System.out.println("Exchanging code for tokens...");
            boolean success = client.exchangeCodeForTokens(authCode);

            if (success && client.accessToken != null) {
                System.out.println("Authentication successful! Testing APIs...");

                // Test current account
                client.getCurrentAccountInfo();

                // Try team info
                client.getTeamInfo();

            } else {
                System.out.println("Authentication failed");
            }

        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static String cleanAuthorizationCode(String input) {
        if (input == null) return "";

        if (input.contains("code=")) {
            input = input.substring(input.indexOf("code=") + 5);
        }
        if (input.contains("&")) {
            input = input.substring(0, input.indexOf("&"));
        }
        if (input.contains("#")) {
            input = input.substring(0, input.indexOf("#"));
        }
        if (input.contains(" ")) {
            input = input.replaceAll(" ", "");
        }

        return input.trim();
    }

}
