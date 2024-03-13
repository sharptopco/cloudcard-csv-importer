package com.onlinephotosubmission.csv_importer;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.stream.Collectors;

public class TokenService {

    private String authToken;

    public void login(String persistentAccessToken, String baseUrl, Proxy proxy) throws IOException {

        String requestBody = "{\"persistentAccessToken\":\"" + persistentAccessToken + "\"}";

            URL url = new URL(baseUrl + "/api/authenticationTokens");
            HttpsURLConnection connection = proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            setConnectionHeaders(connection, "", false);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                System.out.println(getResponseBody(connection, connection.getResponseCode()));
                System.out.println("Logging in to CloudCard API failed - HTTP error code: " + connection.getResponseCode() + ". Exiting application...");
                System.exit(1);
            }

            String response = new BufferedReader(new InputStreamReader((connection.getInputStream()))).readLine();
            authToken = response.split("tokenValue\":\"")[1].split("\"")[0];

            System.out.println("Log in successful - authToken: " + "..." + authToken.substring(3, 8) + "...");

            connection.disconnect();
    }

    public void logout(String baseUrl, Proxy proxy) {
        System.out.println("Logging out authToken: " + "..." + authToken.substring(3, 8) + "...");

        String requestBody = "{\"authenticationToken\":\"" + authToken + "\"}";

        try {

            URL url = new URL(baseUrl + "/api/people/me/logout");
            HttpsURLConnection connection = proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            setConnectionHeaders(connection, authToken, true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();

            if (connection.getResponseCode() != 204) {
                System.out.println(getResponseBody(connection, connection.getResponseCode()));
                System.out.println("Status " + connection.getResponseCode() + " returned from CloudCard API when logging out accessToken.");
                return;
            }

            System.out.println("Status " + connection.getResponseCode() + " returned from CloudCard API when logging out accessToken.");

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setConnectionHeaders(HttpsURLConnection connection, String authToken, boolean includeAuthToken) {

        connection.setRequestProperty("Content-Type", "application/json");
        if (includeAuthToken) connection.setRequestProperty("X-Auth-Token", authToken);
        connection.setRequestProperty("Accept", "application/json");
    }

    private static String getResponseBody(HttpsURLConnection connection, int responseCode) {

        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.lines().collect(Collectors.joining());
        } else {
            return("Error " + responseCode + " : no response body available.");
        }
    }

    public String getAuthToken() {
        return this.authToken;
    }

}
