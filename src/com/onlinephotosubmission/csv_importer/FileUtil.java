package com.onlinephotosubmission.csv_importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by terskine on 7/20/17.
 */
public class FileUtil {

    public static List<String> convertTextFileToListOfLines(File inputFile) throws Exception {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile.getAbsoluteFile().toString()));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

}
