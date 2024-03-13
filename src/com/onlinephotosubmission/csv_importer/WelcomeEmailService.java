package com.onlinephotosubmission.csv_importer;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.stream.Collectors;

public class WelcomeEmailService {

    public static String sendWelcomeEmail(CardHolder cardHolder, String baseUrl, String authToken, Proxy proxy) throws IOException {

        String requestBody = "{\"viewName\":" + "\"welcome\""+ "}";

        URL url = new URL(baseUrl + "/person/" + cardHolder.getEmail() + "/email?findBy=email");
        HttpsURLConnection connection = proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        setConnectionHeaders(connection,authToken);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(requestBody.getBytes());
        outputStream.flush();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
            System.out.println(getResponseBody(connection, connection.getResponseCode()));
            return "Failed : HTTP error code : " + connection.getResponseCode();
        }

        connection.disconnect();

        return "Success";
    }


    private static void setConnectionHeaders(HttpsURLConnection connection, String authToken) {

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Auth-Token", authToken);
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
}
