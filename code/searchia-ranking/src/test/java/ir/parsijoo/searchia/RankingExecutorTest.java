package ir.parsijoo.searchia;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static ir.parsijoo.searchia.RankingPhaseType.*;
import static ir.parsijoo.searchia.SortDirection.ASCENDING;
import static ir.parsijoo.searchia.SortDirection.DESCENDING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
class RankingExecutorTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;

    double totalDuration = 0;
    long maxDuration = 0;

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
                .collect(toList());

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

        docs = docs.stream().sorted().collect(toList());

        assertEquals(7, docs.get(docs.size()-1).getRank());
        assertEquals(5, docs.get(docs.size()-2).getRank());
        assertEquals(4, docs.get(docs.size()-3).getRank());
        assertEquals(3, docs.get(docs.size()-4).getRank());
        assertEquals(1, docs.subList(0, docs.size()-5).stream().map(Doc::getRank).collect(toSet()).size());
    }

    @Test
    void groupDocsByRank() {
        // Set rank of two docs to size - 2 and the rest have default rank of 0 (so two rank groups)
        docs.get(1).setRank(docs.size() - 2);
        docs.get(11).setRank(docs.size() - 2);
        Set<Integer> expectedGroupSizes = Set.of(2, docs.size() - 2);

        SortedMap<Integer, List<Doc>> groups = RankingExecutor.groupDocsByRank(docs);

        Set<Integer> groupSizes = groups.values().stream().map(List::size).collect(toSet());
        assertTrue(groupSizes.containsAll(expectedGroupSizes));
    }

    @Test
    void updateRanks() {
        docs.stream().filter(doc -> doc.getId() < 5).forEach(doc -> doc.setRank(1));
        docs.stream().filter(doc -> doc.getId() % 2 == 0).forEach(doc -> doc.setNumberOfMatches(1));
        List<Integer> expectedRanks = List.of(3, 2, 3, 2, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1);

        RankingExecutor.updateRanks(docs, Doc::getNumberOfMatches, DESCENDING);

        assertThat(docs.stream().map(Doc::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }

    @Test // see https://stackoverflow.com/a/3637974
    void updateRanks_ensureComparingBoxedTypesWithEqualValuesProducesCorrectResult() {
        docs.stream().filter(doc -> doc.getId() < 5).forEach(doc -> doc.setRank(1));
        docs.stream().filter(doc -> doc.getId() % 2 == 0).forEach(doc -> doc.setNumberOfMatches(1));
        docs.stream().filter(doc -> doc.getId() > 10).forEach(doc -> doc.setNumberOfMatches(Integer.MAX_VALUE));
        List<Integer> expectedRanks = List.of(4, 3, 4, 3, 2, 1, 2, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0);

        RankingExecutor.updateRanks(docs, Doc::getNumberOfMatches, DESCENDING);

        assertThat(docs.stream().map(Doc::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }

    @Test
    void executeRanking() throws IOException {
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
        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null),
                new RankingPhase(CUSTOM, true, 5, DESCENDING, "viewCount")
        ));
        List<Integer> expectedDocIdOrder =
                List.of(17, 2, 16, 10, 7, 1, 9, 12, 3, 11, 14, 15, 6, 5, 8, 13, 4).subList(offset, offset + limit);

        List<Doc> result = RankingExecutor.executeRanking(queries, docs, promotions, rankingConfig, offset, limit);

        assertThat(result.stream().map(Doc::getId).collect(toList()), is(equalTo(expectedDocIdOrder)));
    }

    @Test
    void executeRanking_executionTime() throws IOException {
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
        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null),
                new RankingPhase(CUSTOM, true, 5, DESCENDING, "viewCount")
        ));

        Instant startTime = Instant.now();
        RankingExecutor.executeRanking(queries, docs, promotions, rankingConfig, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertThat(duration, is(lessThan(timeThreshold)));
    }

    @Test
    void executeRanking_resultSize() throws IOException {
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
        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null),
                new RankingPhase(CUSTOM, true, 5, DESCENDING, "viewCount")
        ));

        List<Doc> result = RankingExecutor.executeRanking(queries, docs, promotions, rankingConfig, offset, limit);

        assertEquals(limit, result.size());
    }

    @Test
    void executeRanking_useRealData_executionTime() throws IOException, CsvException {
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
                            .collect(toList());
                    return new Doc(0, Map.of(), 0, attributes);
                })
                .collect(toList());

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
        RankingConfig rankingDTO = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null)
        ));

        long timeThreshold = 20/*ms*/;
        Instant startTime = Instant.now();
        RankingExecutor.executeRanking(queries, docs, promotions, rankingDTO, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertThat(duration, is(lessThan(timeThreshold)));
    }

    @RepeatedTest(100)
    void executeRanking_useRealData_averageTimeOf100Executions(RepetitionInfo repetitionInfo) throws IOException, CsvException {
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
                            .collect(toList());
                    return new Doc(0, Map.of(), 0, attributes);
                })
                .collect(toList());

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

        RankingConfig rankingDTO = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null)
        ));

        double timeThreshold = 50.0/*ms*/;
        Instant startTime = Instant.now();
        RankingExecutor.executeRanking(queries, docs, promotions, rankingDTO, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();
        totalDuration += duration;
        maxDuration = Math.max(maxDuration, duration);

        assertThat(totalDuration / repetitionInfo.getCurrentRepetition(), is(lessThan(timeThreshold)));

        if (repetitionInfo.getCurrentRepetition() == repetitionInfo.getTotalRepetitions()) {
            System.out.println("Average execution time: " + totalDuration / repetitionInfo.getTotalRepetitions() + " ms");
            System.out.println("Max execution time: " + maxDuration + " ms");
        }
    }
}
