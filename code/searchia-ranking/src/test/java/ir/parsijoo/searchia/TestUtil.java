package ir.parsijoo.searchia;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class TestUtil {

    private static final Path sampleRecordsPath = Paths.get("src/test/resources/sample-records.txt");
    private static final Path realRecordsPath = Paths.get("src/test/resources/real-records.csv");

    public static List<Record> createSampleRecords() throws IOException {
        return Files
                .lines(sampleRecordsPath)
                .filter(line -> !line.startsWith("#"))
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    double score = Math.random();
                    double creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    double viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    String title = attrs[3].split("=")[1];
                    String description = attrs[4].split("=")[1];
                    Map<String, String> searchableAttrs = Stream.of(
                            new SimpleEntry<>("title", title),
                            new SimpleEntry<>("description", description)
                    ).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
                    Map<String, Double> customAttrs = Stream.of(
                            new SimpleEntry<>("viewCount", viewCount),
                            new SimpleEntry<>("creationDate", creationDate)
                    ).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
                    return new Record(id, customAttrs, score, searchableAttrs);
                })
                .collect(toList());
    }

    public static List<Record> createRealRecords() throws IOException, CsvException {
        File file = realRecordsPath.toFile();
        CSVParser csvParser = new CSVParserBuilder().withIgnoreQuotations(true).build();
        CSVReader csvReader = new CSVReaderBuilder(new FileReader(file))
                .withSkipLines(1)
                .withCSVParser(csvParser)
                .build();
        List<String[]> records = csvReader.readAll();

        return records.stream()
                .map(fields -> {
                    Map<String, String> searchableAttributes = Arrays.stream(fields)
                            .filter(field -> field.startsWith("{\"anchorText\":") || field.startsWith("\"title\":\""))
                            .map(field -> {
                                if (field.startsWith("{\"")) {
                                    field = field.substring(1);
                                } else if (field.endsWith("\"}")) {
                                    field = field.substring(0, field.length() - 1);
                                }
                                String[] fieldParts = field.split("\":\"");
                                String attrName = fieldParts[0].substring(1);
                                String attrValue = fieldParts[1].substring(0, fieldParts[1].length() - 1);

                                return new SimpleEntry<>(attrName, attrValue);
                            })
                            .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue));

                    Map<String, ? extends Comparable<?>> customAttributes = Stream.of(
                            new SimpleEntry<>("clicks", (double) new Random().nextInt(100)),
                            new SimpleEntry<>( "score", new Random().nextDouble() * 5)
                    ).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

                    return new Record(0, customAttributes, 0, searchableAttributes);
                })
                .collect(toList());

    }
}
