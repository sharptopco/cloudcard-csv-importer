package com.onlinephotosubmission.csv_importer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by terskine on 7/20/17.
 */
public class FileUtil {

    public static List<String> convertTextFileToListOfLines(File inputFile) throws Exception {

        System.out.println("Using the default character set, " + Charset.defaultCharset().displayName() + ", to read " + inputFile.getName());

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile.getAbsoluteFile().toString()));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

    public static List<String> convertTextFileToListOfLines(File inputFile, String charset) throws Exception {

        System.out.println("Using " + charset + " to read " + inputFile.getName());

        String fileName = inputFile.getAbsoluteFile().toString();
        FileInputStream fileInputStream = new FileInputStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName(charset)));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

}
