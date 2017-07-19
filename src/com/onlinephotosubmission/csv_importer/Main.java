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

    public static String[] headerTypes = {"Email", "ID", "Campus", "Notes"};
    private static String delimiter = ",";

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("config.properties"));
        File[] inputFiles = getCSVFilesFromDirectory(properties.getProperty("InputFile"));

        for (File inputFile : inputFiles) {
            String fileName = removeFileNameExtension(inputFile);
            List<String> lines = convertTextFileToListOfLines(inputFile.getAbsoluteFile().toString(), fileName, properties.getProperty("ReportFile"));
            List<CardHolder> cardHolders = convertLinesIntoCardHolders(lines);
            saveCardHolders(cardHolders, fileName, properties.getProperty("ReportFile"), properties);
            transferFileToCompleted(inputFile, properties.getProperty("CompletedFile"));
        }
    }

    private static String[] readPropertyFileIntoLocalVariables(String[] args, String[] fileLocations, Properties properties) throws IOException {

        if (args.length == 0) {
//            savePropertyFileToArguments(properties, fileLocations);
        } else {
            fileLocations[ 0 ] = args[ 0 ];
            fileLocations[ 1 ] = args[ 1 ];
            fileLocations[ 2 ] = args[ 2 ];
        }
        return fileLocations;
    }

    private static String transferToCloudCard(CardHolder cardHolder, Properties properties) {

        try {

            URL url = new URL(properties.getProperty("URL") + "/api/people");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("X-Auth-Token", properties.getProperty("AccessToken"));
            connection.setRequestProperty("Accept", "application/json");

            String input = "{ \"email\":\"" + cardHolder.getEmail() + "\"," + "\"organization\":{\"id\":" + properties.getProperty("ID") + "}," + "\"customFields\":{" + "\"Campus\":\"" + cardHolder.getCampus() + "\"," + "\"Notes\":\"" + cardHolder.getNotes() + "\"}, " + "\"identifier\":\"" + cardHolder.getID() + "\" }";
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

    private static String removeFileNameExtension(File csvfile) {

        return csvfile.getName().replaceFirst("[.][^.]+$", "");
    }

    private static File[] getCSVFilesFromDirectory(String filePath) {

        File dir = new File(filePath);
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir1, String name) {

                return name.endsWith(".csv");
            }
        });
    }

    private static String getFileNameWithTimeStamp(String fileName) {

        LocalDateTime timeStamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH" + "\u02f8" + "mm" + "\u02f8" + "ss");
        String formatDateTime = timeStamp.format(formatter);
        return fileName + "-" + formatDateTime + "-Report.csv";
    }

    private static void transferFileToCompleted(File inputFile, String completedFile) {

        try {
            Files.move(Paths.get(inputFile.getAbsoluteFile().toString()), Paths.get(completedFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveCardHolders(List<CardHolder> cardHolders, String fileName, String fileLocation, Properties properties) {

        String content = "";
        String header = "Status" + ", " + cardHolders.get(0) + "\n";
        for (CardHolder cardHolder : dropHeaderFromList(cardHolders)) {

            String result;
            if (!cardHolder.validate()) {
                result = "failed validation";
            } else {
                result = transferToCloudCard(cardHolder, properties);
            }
            content = content + result + ", " + cardHolder + "\n";
        }
        try {
            String reportOutputPath = fileLocation + "/" + getFileNameWithTimeStamp(fileName);
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

    private static List<String> convertTextFileToListOfLines(String csvPath, String fileName, String arg) throws Exception {

        List<String> lines = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
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
            String reportOutputPath = arg + "/" + getFileNameWithTimeStamp(fileName);
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
