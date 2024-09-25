package com.illumio;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class Solution {
    private static final String DEFAULT_PROTOCOLS_FILEPATH = "protocols.txt";
    private static final String DEFAULT_LOGS_FILEPATH = "sample.log";
    private static final String DEFAULT_LOOKUP_CSV_FILEPATH = "lookup.csv";
    private static final String DEFAULT_FIELD_SEPARATOR = "\\s+";
    private static final String DEFAULT_TAGGING_STATS_FILEPATH = "tagging_stats.csv";
    private static final String DEFAULT_MATCHING_STATS_FILEPATH = "matching_stat.csv";
    private static final String COMMA_DELIMITER = ",";
    private static final String TAB_DELIMITER = "\t";
    private static final String UNTAGGED_NAME = "Untagged";

    public static void main(String[] args) {
        String logsFilepath = args.length < 1 ? DEFAULT_LOGS_FILEPATH : args[0];
        String lookupCsvFilepath = args.length < 2 ? DEFAULT_LOOKUP_CSV_FILEPATH : args[1];
        String fieldSeparator = args.length < 3 ? DEFAULT_FIELD_SEPARATOR : args[2];
        String taggingStatsFilepath = args.length < 4 ? DEFAULT_TAGGING_STATS_FILEPATH : args[3];
        String matchingStatsFilepath = args.length < 5 ? DEFAULT_MATCHING_STATS_FILEPATH : args[4];

        Solution solution = new Solution();
        solution.calculateStatistics(logsFilepath, lookupCsvFilepath, fieldSeparator, taggingStatsFilepath, matchingStatsFilepath);
    }

    public void calculateStatistics(
            String logsFilepath,
            String lookupCsvFilepath,
            String fieldSeparator,
            String taggingStatsFilepath,
            String matchingStatsFilepath
    ) {
        Map<String, String> decimalToProtocol = loadProtocols(DEFAULT_PROTOCOLS_FILEPATH);
        Map<TagMappingProps, String> tagMappings = loadTagMappings(lookupCsvFilepath);
        Map<TagMappingProps, Integer> foundPropsFrequencies = new HashMap<>();
        Map<String, Integer> matchedTagFrequencies = new HashMap<>();

        readFileByLines(logsFilepath, false, logLine -> {
            String[] logFields = logLine.trim().split(fieldSeparator);
            TagMappingProps props = TagMappingProps.fromLogFields(logFields, decimalToProtocol);

            foundPropsFrequencies.merge(props, 1, Integer::sum);

            String matchedTag = tagMappings.getOrDefault(props, UNTAGGED_NAME);
            matchedTagFrequencies.merge(matchedTag, 1, Integer::sum);
        });

        writeFoundPropsFrequencies(taggingStatsFilepath, foundPropsFrequencies);
        writeMatchedTagFrequencies(matchingStatsFilepath, matchedTagFrequencies);
    }

    private Map<String, String> loadProtocols(String filepath) {
        Map<String, String> protocols = new HashMap<>();

        readFileByLines(filepath, false, line -> {
            String[] fields = line.split(TAB_DELIMITER);
            protocols.put(fields[0], fields[1].toLowerCase(Locale.ROOT));
        });

        return protocols;
    }

    private Map<TagMappingProps, String> loadTagMappings(String filepath) {
        Map<TagMappingProps, String> tags = new HashMap<>();

        readFileByLines(filepath, true, line -> {
            String[] fields = line.split(COMMA_DELIMITER);
            tags.put(new TagMappingProps(fields[0], fields[1]), fields[2]);
        });

        return tags;
    }

    private void writeFoundPropsFrequencies(String filepath, Map<TagMappingProps, Integer> foundPropsFrequencies) {
        int linesAmount = foundPropsFrequencies.size();

        String[] columnDescription = new String[]{"Port", "Protocol", "Count"};
        String[][] data = new String[linesAmount + 1][columnDescription.length];
        data[0] = columnDescription;

        int i = 1;
        for (var entry : foundPropsFrequencies.entrySet()) {
            TagMappingProps props = entry.getKey();
            data[i++] = new String[]{props.dstport(), props.protocol(), entry.getValue().toString()};
        }

        writeCsv(filepath, data);
    }

    private void writeMatchedTagFrequencies(String filepath, Map<String, Integer> foundTagFrequencies) {
        int linesAmount = foundTagFrequencies.size();

        String[] columnDescription = new String[]{"Tag", "Count"};
        String[][] data = new String[linesAmount + 1][columnDescription.length];
        data[0] = columnDescription;

        int i = 1;
        for (var entry : foundTagFrequencies.entrySet()) {
            data[i++] = new String[]{entry.getKey(), entry.getValue().toString()};
        }

        writeCsv(filepath, data);
    }

    private void writeCsv(String filepath, String[][] data) {
        try (PrintWriter pw = new PrintWriter(filepath)) {
            for (String[] fields : data) {
                String line = String.join(",", fields);
                pw.println(line);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Reading file line by line, feeding line consumer with this lines
    private void readFileByLines(String filepath, boolean skipFirstLine, Consumer<String> lineConsumer) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            if (skipFirstLine) {
                br.readLine();
            }
            while ((line = br.readLine()) != null) {
                lineConsumer.accept(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
