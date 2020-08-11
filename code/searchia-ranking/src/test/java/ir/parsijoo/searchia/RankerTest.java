package ir.parsijoo.searchia;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class RankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    RankConfiguration configuration;

    @BeforeEach
    void setUp() throws IOException {
        query = "dodge charger";

        docs = Files
                .lines(samplesPath)
                .filter(line -> !line.startsWith("#"))
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    double creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    double viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    double score = Math.random();
                    Attribute<String> title = new Attribute<>(attrs[3].split("=")[0], attrs[3].split("=")[1]);
                    Attribute<String> description = new Attribute<>(attrs[4].split("=")[0], attrs[4].split("=")[1]);
                    List<Attribute<String>> searchableAttrs = List.of(title, description);
                    Map<String, Double> customAttrs = Map.of("viewCount", viewCount, "creationDate", creationDate);
                    return new Doc(id, customAttrs, score, searchableAttrs);
                })
                .collect(Collectors.toList());

        configuration = new RankConfiguration(
                "price",
                null,
                false,
                List.of("viewCount", "creationDate"),
                Set.of("dodge")
        );

        promotions = List.of(
                new Promotion(),
                new Promotion()
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void sortDocs() {
        docs.get(0).setRank(5);
        docs.get(2).setRank(3);
        docs.get(10).setRank(7);
        docs.get(12).setRank(4);

        docs = docs.stream().sorted().collect(Collectors.toList());

        assertEquals(7, docs.get(docs.size()-1).getRank());
        assertEquals(5, docs.get(docs.size()-2).getRank());
        assertEquals(4, docs.get(docs.size()-3).getRank());
        assertEquals(3, docs.get(docs.size()-4).getRank());
        assertEquals(1, docs.subList(0, docs.size()-5).stream().map(Doc::getRank).collect(Collectors.toSet()).size());
    }

    @Test
    void rank() throws IOException {
        int offset = 0;
        int limit = 10;
        Query query1 = new Query("dodge charter", Query.QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", Query.QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", Query.QueryType.SUGGESTED);
        Map<Query.QueryType, Query> queries = Map.of(
                Query.QueryType.ORIGINAL, query1,
                Query.QueryType.WILDCARD, query2,
                Query.QueryType.SUGGESTED, query3
        );
        List<Integer> expectedDocIdOrder =
                List.of(17, 2, 16, 10, 7, 1, 9, 12, 3, 11, 14, 15, 6, 5, 8, 13, 4).subList(offset, offset + limit);

        List<Doc> result = Ranker.rank(queries, docs, promotions, configuration, offset, limit);

        assertThat(result.stream().map(Doc::getId).collect(Collectors.toList()), is(equalTo(expectedDocIdOrder)));
    }

    @Test
    void rank_executionTime() throws IOException {
        long timeThreshold = 50/*ms*/;
        int offset = 0;
        int limit = 10;
        Query query1 = new Query("dodge charter", Query.QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", Query.QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", Query.QueryType.SUGGESTED);
        Map<Query.QueryType, Query> queries = Map.of(
                Query.QueryType.ORIGINAL, query1,
                Query.QueryType.WILDCARD, query2,
                Query.QueryType.SUGGESTED, query3
        );

        Instant startTime = Instant.now();
        Ranker.rank(queries, docs, promotions, configuration, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertThat(duration, is(lessThan(timeThreshold)));
    }

    @Test
    void rank_resultSize() throws IOException {
        int offset = 0;
        int limit = 10;
        Query query1 = new Query("dodge charter", Query.QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", Query.QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", Query.QueryType.SUGGESTED);
        Map<Query.QueryType, Query> queries = Map.of(
                Query.QueryType.ORIGINAL, query1,
                Query.QueryType.WILDCARD, query2,
                Query.QueryType.SUGGESTED, query3
        );

        List<Doc> result = Ranker.rank(queries, docs, promotions, configuration, offset, limit);

        assertEquals(limit, result.size());
    }

    @Test
    void rank_useRealData() throws IOException, CsvException {
        File file = new File("src/test/resources/real-docs.csv");
        CSVParser csvParser = new CSVParserBuilder().withIgnoreQuotations(true).build();
        CSVReader csvReader = new CSVReaderBuilder(new FileReader(file))
                .withSkipLines(1)
                .withCSVParser(csvParser)
                .build();
        List<String[]> records = csvReader.readAll();

        List<Doc> docs = records.stream()
                .map(fields -> {
                    List<Attribute<String>> attributes = Arrays.stream(fields)
                            .filter(field -> field.startsWith("{\"anchorText\":") || field.startsWith("\"title\":\""))
                            .map(field -> {
                                if (field.startsWith("{\"")) {
                                    field = field.substring(1);
                                } else if (field.endsWith("\"}")) {
                                    field = field.substring(0, field.length() - 1);
                                }
                                String[] fieldParts = field.split("\":\"");
                                fieldParts[0] = fieldParts[0].substring(1);
                                fieldParts[1] = fieldParts[1].substring(0, fieldParts[1].length() - 1);

                                return new Attribute<>(fieldParts[0], fieldParts[1]);
                            })
                            .collect(Collectors.toList());
                    return new Doc(0, Map.of(), 0, attributes);
                })
                .collect(Collectors.toList());

        RankConfiguration configuration = new RankConfiguration(
                "fake",
                null,
                false,
                List.of(),
                Set.of("fake")
        );

        int offset = 0;
        int limit = 10;
        Query query1 = new Query("معرفی فیل", Query.QueryType.ORIGINAL);
        Query query2 = new Query("معرفی فیل*", Query.QueryType.WILDCARD);
        Query query3 = new Query("معرفی فیلم", Query.QueryType.CORRECTED);
        Map<Query.QueryType, Query> queries = Map.of(
                Query.QueryType.ORIGINAL, query1,
                Query.QueryType.WILDCARD, query2,
                Query.QueryType.CORRECTED, query3
        );

        long timeThreshold = 20/*ms*/;
        Instant startTime = Instant.now();
        List<Doc> result = Ranker.rank(queries, docs, promotions, configuration, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertThat(duration, is(lessThan(timeThreshold)));
    }
}
