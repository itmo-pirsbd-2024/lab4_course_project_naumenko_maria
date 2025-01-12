package ru.nms.diplom.ircrossfeature.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataFrameLoader {

    public static Map<String, String> loadRelevantPassages(String filePath){
        Map<String, String> relevantPassages = new HashMap<>();

        try (FileReader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {
            for (CSVRecord csvRecord : csvParser) {
                String key = csvRecord.get("query");
                String value = csvRecord.get("relevant_passage_id");
                relevantPassages.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return relevantPassages;
    }
}
