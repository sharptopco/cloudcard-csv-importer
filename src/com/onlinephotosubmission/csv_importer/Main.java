package com.onlinephotosubmission.csv_importer;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Main {

    public static String[] headerTypes = {
            "Email",
            "ID",
            "Campus",
            "Notes"
    };
    private static String delimiter = ",";

    public static void main(String[] args) throws Exception {
        String[] fileLocations = new String[3];
        Properties properties = new Properties();
        readPropertyFileIntoLocalArguments(args, fileLocations, properties);
        File[] files = getCSVFilesFromDirectory(fileLocations[0]);

        for (File csvfile : files) {
            String fileName = removeFileNameExtension(csvfile);
            List<String> lines = convertTextFileToListOfLines(csvfile.getAbsoluteFile().toString(), fileName, fileLocations[2]);
            List<CardHolder> cardHolders = convertLinesIntoCardHolders(lines);
            saveCardHolders(cardHolders, fileName, fileLocations[2], properties);
            Path inputFile = Paths.get(csvfile.getAbsoluteFile().toString());
            Path completedFile = Paths.get(fileLocations[1]);
            transferFileToCompleted(inputFile, completedFile);
        }
    }

    private static void readPropertyFileIntoLocalArguments(String[] args, String[] fileLocations, Properties properties) throws IOException {
        properties.load(new FileInputStream("config.properties"));
        if (args.length == 0) {
            savePropertyFileToArguments(properties, fileLocations);
        } else {
            fileLocations[0] = args[0];
            fileLocations[1] = args[1];
            fileLocations[2] = args[2];
        }
    }

    private static void savePropertyFileToArguments(Properties properties, String[] fileLocations) throws IOException {
        fileLocations[0] = properties.getProperty("InputFile");
        fileLocations[1] = properties.getProperty("CompletedFile");
        fileLocations[2] = properties.getProperty("ReportFile");
    }

    private static String transferToCloudCard(CardHolder cardHolder, Properties properties) {
        try {

            URL url = new URL(properties.getProperty("URL"));
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("X-Auth-Token", properties.getProperty("AccessToken"));
            connection.setRequestProperty("Accept", "application/json");

            String input = "{ \"email\":\"" + cardHolder.getEmail() + "\"," +
                    "\"organization\":{\"id\":" + properties.getProperty("ID") + "}," +
                    "\"customFields\":{" +
                    "\"Campus\":\"" + cardHolder.getCampus() + "\"," +
                    "\"Notes\":\"" + cardHolder.getNotes() + "\"}, " +
                    "\"identifier\":\"" + cardHolder.getID() + "\" }";
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

    private static void transferFileToCompleted(Path inputFile, Path completedFile) {
        try {
            Files.move(inputFile, completedFile.resolve(inputFile.getFileName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveCardHolders(List<CardHolder> cardHolders, String fileName, String fileLocation, Properties properties) {
        String content = "";
        String header = "Status" + ", " + cardHolders.get(0) + "\n";
        for(CardHolder cardHolder : dropHeaderFromList(cardHolders)) {

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
                if(headerTypes[j].equals(Header[i])) {
                    if(i == 0) { CardHolder.setEmailIndex(j); }
                    if(i == 1) { CardHolder.setIdIndex(j); }
                    if(i == 2) { CardHolder.setCampusIndex(j); }
                    if(i == 3) { CardHolder.setNotesIndex(j); }
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

    private static List<String> convertTextFileToListOfLines(String csvPath, String fileName, String arg) throws Exception{
        List<String> lines = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
            lines = new ArrayList<String>();
            String line;
            int initialHeaderRead = 0;
            while((line = bufferedReader.readLine()) != null) {
                if(initialHeaderRead == 0) {
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
