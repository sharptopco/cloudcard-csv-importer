package com.onlinephotosubmission.csv_importer;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    public static final String CONFIG_PROPERTIES = "config.properties";
    public static final String INPUT_DIR = "input.directory";
    public static final String REPORT_DIR = "report.directory";
    public static final String COMPLETED_DIR = "completed.directory";
    public static final String ACCESS_TOKEN = "access.token";
    public static final String ORG_ID = "organization.id";
    public static final String BASE_URL = "base.url";
    public static String[] headerTypes = {"Email", "ORG_ID", "Campus", "Notes"};
    private static String delimiter = ",";

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        properties.load(new FileInputStream(CONFIG_PROPERTIES));

        for (File inputFile : loadInputFiles(properties)) {
            List<String> lines = convertTextFileToListOfLines(inputFile, properties);
            List<CardHolder> cardHolders = convertLinesIntoCardHolders(lines);
            saveCardHolders(cardHolders, inputFile, properties);
            moveFileToCompleted(inputFile, properties);
        }
    }

    private static String importToCloudCard(CardHolder cardHolder, Properties properties) {

        try {

            URL url = new URL(properties.getProperty(BASE_URL) + "/api/people");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            setConnectionHeaders(connection, properties);

            String input = "{ \"email\":\"" + cardHolder.getEmail() + "\"," + "\"organization\":{\"id\":" + properties.getProperty(ORG_ID) + "}," + "\"customFields\":{" + "\"Campus\":\"" + cardHolder.getCampus() + "\"," + "\"Notes\":\"" + cardHolder.getNotes() + "\"}, " + "\"identifier\":\"" + cardHolder.getID() + "\" }";
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(input.getBytes());
            outputStream.flush();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                return "Failed : HTTP error code : " + connection.getResponseCode();
            }

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Success";
    }

    private static void setConnectionHeaders(HttpsURLConnection connection, Properties properties) {

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Auth-Token", properties.getProperty(ACCESS_TOKEN));
        connection.setRequestProperty("Accept", "application/json");
    }

    private static String stripFileExtension(File csvfile) {

        return csvfile.getName().replaceFirst("[.][^.]+$", "");
    }

    private static File[] loadInputFiles(Properties properties) {

        File dir = new File(properties.getProperty(INPUT_DIR));
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir1, String name) {

                return name.endsWith(".csv");
            }
        });
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

    private static void saveCardHolders(List<CardHolder> cardHolders, File inputFile, Properties properties) {

        String content = "";
        String header = "Status" + ", " + cardHolders.get(0) + "\n";
        for (CardHolder cardHolder : dropHeaderFromList(cardHolders)) {

            String result;
            if (!cardHolder.validate()) {
                result = "failed validation";
            } else {
                result = importToCloudCard(cardHolder, properties);
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

    private static List<CardHolder> dropHeaderFromList(List<CardHolder> cardHolders) {

        return cardHolders.subList(1, cardHolders.size());
    }

    public static void calculateIndexForOutput(String header) {

        String[] Header = header.split(delimiter);
        for (int i = 0; i < Header.length; i++) {
            for (int j = 0; j < headerTypes.length; j++) {
                if (headerTypes[ j ].equals(Header[ i ])) {
                    if (i == 0) { CardHolder.setEmailIndex(j); }
                    if (i == 1) { CardHolder.setIdIndex(j); }
                    if (i == 2) { CardHolder.setCampusIndex(j); }
                    if (i == 3) { CardHolder.setNotesIndex(j); }
                }
            }
        }
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
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile.getAbsoluteFile().toString()));
            lines = new ArrayList<String>();
            String line;
            int initialHeaderRead = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (initialHeaderRead == 0) {
                    calculateIndexForOutput(line);
                    initialHeaderRead++;
                }
                lines.add(line);
            }
            bufferedReader.close();
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
