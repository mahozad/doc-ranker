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
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class TestUtil {

    private static final Path sampleDocsPath = Path.of("src/test/resources/sample-docs.txt");
    private static final Path realDocsPath = Path.of("src/test/resources/real-docs.csv");

    public static List<Doc> createSampleDocs() throws IOException {
        return Files
                .lines(sampleDocsPath)
                .filter(line -> !line.startsWith("#"))
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    double score = Math.random();
                    double creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    double viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    String title = attrs[3].split("=")[1];
                    String description = attrs[4].split("=")[1];
                    Map<String, String> searchableAttrs = Map.of("title", title, "description", description);
                    Map<String, Double> customAttrs = Map.of("viewCount", viewCount, "creationDate", creationDate);
                    return new Doc(id, customAttrs, score, searchableAttrs);
                })
                .collect(toList());
    }

    public static List<Doc> createRealDocs() throws IOException, CsvException {
        File file = realDocsPath.toFile();
        CSVParser csvParser = new CSVParserBuilder().withIgnoreQuotations(true).build();
        CSVReader csvReader = new CSVReaderBuilder(new FileReader(file))
                .withSkipLines(1)
                .withCSVParser(csvParser)
                .build();
        List<String[]> records = csvReader.readAll();

        return records.stream()
                .map(fields -> {
                    Map<String, String> attributes = Arrays.stream(fields)
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
                    return new Doc(0, Map.of(), 0, attributes);
                })
                .collect(toList());

    }
}
