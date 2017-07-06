package com.onlinephotosubmission.csv_importer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main {

    public static String[] headerTypes = {
            "Email",
            "ID",
            "Campus",
            "Notes"
    };
    private static String delimiter = ",";

    public static void main(String[] args) throws Exception {

        String filePath = args[0];
        String completedFile = args[1];
        File[] files = getCSVFilesFromDirectory(filePath);

        for (File csvfile : files) {
            String fileName = removeFileNameExtension(csvfile);
            List<String> lines = convertTextFileToListOfLines(csvfile.getAbsoluteFile().toString());
            List<CardHolder> cardHolders = convertLinesIntoCardHolders(lines);
            saveCardHolders(cardHolders, fileName, args[2]);
            Path inFile = Paths.get(csvfile.getAbsoluteFile().toString());
            Path outputFile = Paths.get(completedFile);
//            transferFileToCompleted(inFile, outputFile);
        }
//        transferDataToCloudCard();
    }

    private static void transferDataToCloudCard() {
        try {

            URL url = new URL("https://test.cloudcardtools.com/api/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String input = "{\"username\":\"test@test.com\",\"password\":\"test123\"}";

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... ");
            while ((output = br.readLine()) != null) {
                System.out.println(output + "\n");
            }

            conn.disconnect();

        } catch (IOException e) {

            e.printStackTrace();

        }
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

    private static String fileName(String fileName) {
        LocalDate timeStamp = LocalDate.now();
        return fileName + "-" + timeStamp + "-Report.csv";
    }

    private static void transferFileToCompleted(Path inputFile, Path outputFile) {
        try {
            Files.move(inputFile, outputFile.resolve(inputFile.getFileName()), REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveCardHolders(List<CardHolder> cardHolders, String fileName, String arg) {
        String content = "";
        String header = "";
        header = "Status" + ", " + cardHolders.get(0) + "\n";
        for(CardHolder cardHolder : dropHeaderFromList(cardHolders)) {

            String result = "output message";
            if (!cardHolder.validate()) {
                result = "failed validation";
            } else {
                //TODO: call the webservice (https://test.cloudcardtools.com/api/login)
                transferDataToCloudCard();
                result = "success";
            }
            content = content + result + ", " + cardHolder + "\n";
        }
        try {
            String reportOutputPath = arg + "/" + fileName(fileName);
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

    private static List<String> convertTextFileToListOfLines(String csvPath) throws Exception{
        List<String> lines = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
            lines = new ArrayList<String>();
            String line = null;
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
            //TODO: Write output file with "Failed to read input file\n + e.message\n e.stackTrace"
            throw new Exception(e);
        }
        return lines;
    }
}
