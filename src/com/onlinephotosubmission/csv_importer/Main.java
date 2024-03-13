package com.onlinephotosubmission.csv_importer;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Main {

    public static final String CONFIG_PROPERTIES = "config.properties";
    public static final String INPUT_DIR = "input.directory";
    public static final String REPORT_DIR = "report.directory";
    public static final String COMPLETED_DIR = "completed.directory";
    public static final String PERSISTENT_ACCESS_TOKEN = "access.token";
    public static final String BASE_URL = "base.url";
    public static final String CHARACTER_SET = "character.set";
    public static final String SEND_EMAIL_IF_EXISTS = "sendEmailIfExists";
    public static final String UPDATE_IF_USER_EXISTS = "updateIfUserExists";
    public static final String delimiter = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    public static final String PROXY_HOST = "proxy.host";
    public static final String PROXY_PORT = "proxy.port";

    public static void main(String[] args) throws Exception {
        TokenService tokenService = new TokenService();
        Properties properties = new Properties();

        try {
            if (args.length > 0) {
                properties.load(new FileInputStream(args[ 0 ]));
            } else {
                properties.load(new FileInputStream(CONFIG_PROPERTIES));
            }
        } catch (FileNotFoundException e) {
            System.err.println("*** ERROR: Filed to load properties file. Caused by: " + e.getMessage() + " ***");
            return;
        }

        if (properties.get(SEND_EMAIL_IF_EXISTS) == null) {
            properties.setProperty(SEND_EMAIL_IF_EXISTS, "true");
        }

        if (properties.get(UPDATE_IF_USER_EXISTS) == null) {
            properties.setProperty(UPDATE_IF_USER_EXISTS, "true");
        }

        System.out.println("Properties Loaded --> " + properties);

        Proxy proxy = null;

        if (properties.getProperty(PROXY_HOST) != null && properties.getProperty(PROXY_PORT) != null) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(properties.getProperty(PROXY_HOST), Integer.parseInt(properties.getProperty(PROXY_PORT))));
        }

        tokenService.login(properties.getProperty(PERSISTENT_ACCESS_TOKEN), properties.getProperty(BASE_URL), proxy);
        for (File inputFile : loadInputFiles(properties)) {
            List<String> lines = convertTextFileToListOfLines(inputFile, properties);
            List<CardHolder> cardHolders = convertLinesIntoCardHolders(lines);
            saveCardHolders(cardHolders, inputFile, properties, tokenService.getAuthToken(), proxy);
            moveFileToCompleted(inputFile, properties);
        }
        tokenService.logout(properties.getProperty(BASE_URL), proxy);
    }

    private static String importToCloudCard(CardHolder cardHolder, Properties properties, String authToken, Proxy proxy) {

        try {
            URL url = new URL(properties.getProperty(BASE_URL) + "/api/people");

            HttpsURLConnection connection = proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            setConnectionHeaders(connection, authToken);

            OutputStream outputStream = connection.getOutputStream();
            String json = cardHolder.toJSON();
            System.out.println("json: " + json);
            outputStream.write(json.getBytes());
            outputStream.flush();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                String result = "Failed : HTTP error code : " + connection.getResponseCode();

                if (properties.getProperty(UPDATE_IF_USER_EXISTS).equals("true")) {
                    result = updateInCloudCard(cardHolder,properties,authToken, proxy);

                    if (!result.equals("Success")) return result;
                }

                if (properties.getProperty(SEND_EMAIL_IF_EXISTS).equals("true")) {
                    return WelcomeEmailService.sendWelcomeEmail(cardHolder, properties.getProperty(BASE_URL), authToken, proxy);
                }

                return result;

            } else if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                return "Failed : HTTP error code : " + connection.getResponseCode();
            }

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Success";
    }

    private static String updateInCloudCard(CardHolder cardHolder, Properties properties, String authToken, Proxy proxy) {

        try {

            URL url = new URL(properties.getProperty(BASE_URL) + "/api/people/" + cardHolder.getId() + "?findBy=identifier&updateRoles=false");
            HttpsURLConnection connection = proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy);
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            setConnectionHeaders(connection, authToken);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(cardHolder.toJSON(true).getBytes());
            outputStream.flush();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                System.out.println(getResponseBody(connection, connection.getResponseCode()));
                return "Failed : HTTP error code : " + connection.getResponseCode();
            }

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Success";
    }

    private static String getResponseBody(HttpsURLConnection connection, int responseCode) {

        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.lines().collect(Collectors.joining());
        } else {
            String response = "Error " + responseCode + " : no response body available.";
            return response;
        }
    }

    private static void setConnectionHeaders(HttpsURLConnection connection, String authToken) {

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Auth-Token", authToken);
        connection.setRequestProperty("Accept", "application/json");
    }

    private static String stripFileExtension(File csvfile) {

        return csvfile.getName().replaceFirst("[.][^.]+$", "");
    }

    private static File[] loadInputFiles(Properties properties) throws FileNotFoundException {

        File dir = new File(properties.getProperty(INPUT_DIR));
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir1, String name) {

                return name.endsWith(".csv");
            }
        });
        if (files == null) {
            throw new FileNotFoundException("Input directory not found.");
        } else if (files.length == 0) {
            System.out.println("No input file found.");
        }
        return files;
    }

    private static String createReportFileName(File inputFile) {

        LocalDateTime timeStamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH" + "\u02f8" + "mm" + "\u02f8" + "ss");
        String formatDateTime = timeStamp.format(formatter);
        return stripFileExtension(inputFile) + "-" + formatDateTime + "-Report.csv";
    }

    private static void moveFileToCompleted(File inputFile, Properties properties) {

        String completedFile = properties.getProperty(COMPLETED_DIR);
        try {
            Files.move(Paths.get(inputFile.getAbsoluteFile().toString()), Paths.get(completedFile + "/" + inputFile.getName()));
        } catch (IOException e) {
            System.out.println("Failed to move " + inputFile.getAbsoluteFile().toString() + " to " + completedFile);
            e.printStackTrace();
        }
    }

    private static void saveCardHolders(List<CardHolder> cardHolders, File inputFile, Properties properties, String authToken, Proxy proxy) {

        boolean updateCardholders = inputFile.getName().toLowerCase().contains("update");

        String content = "";
        String header = "Status" + ", " + CardHolder.csvHeader() + "\n";
        for (CardHolder cardHolder : cardHolders) {

            String result;
            if (!cardHolder.validate()) {
                result = "failed validation";
            } else {
                if (updateCardholders) {
                    result = "UPDATE " + updateInCloudCard(cardHolder, properties, authToken, proxy);
                } else {
                    result = "CREATE " + importToCloudCard(cardHolder, properties, authToken, proxy);
                }
            }
            content = content + result + ", " + cardHolder + "\n";
        }
        try {
            String reportOutputPath = properties.getProperty(REPORT_DIR) + "/" + createReportFileName(inputFile);
            File file = new File(reportOutputPath);

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(header);
            bufferedWriter.write(content);

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> dropHeaderFromList(List<String> lines) {

        return lines.subList(1, lines.size());
    }

    private static List<CardHolder> convertLinesIntoCardHolders(List<String> lines) {

        List<CardHolder> cardHolders = new ArrayList<CardHolder>();
        for (String line : lines) {
            cardHolders.add(convertLineIntoCardholder(line));
        }
        return cardHolders;
    }

    private static CardHolder convertLineIntoCardholder(String line) {

        CardHolder cardHolder = new CardHolder(delimiter, line);
        return cardHolder;
    }

    private static List<String> convertTextFileToListOfLines(File inputFile, Properties properties) throws Exception {

        List<String> lines = null;
        try {
            String charset = properties.getProperty(CHARACTER_SET);
            if (charset != null && !charset.isEmpty())
                lines = FileUtil.convertTextFileToListOfLines(inputFile, charset);
            else
                lines = FileUtil.convertTextFileToListOfLines(inputFile);
            CardHolder.setHeader(lines.get(0).split(delimiter));
            lines = dropHeaderFromList(lines);
        } catch (IOException e) {
            String reportOutputPath = properties.getProperty(REPORT_DIR) + "/" + createReportFileName(inputFile);
            String failedRead = "Failed to read input file \n" + e.getMessage();
            File file = new File(reportOutputPath);

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(failedRead);
            e.printStackTrace();

            bufferedWriter.close();
            throw new Exception(e);
        }
        return lines;
    }
}
