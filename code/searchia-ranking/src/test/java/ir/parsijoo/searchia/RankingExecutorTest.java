package ir.parsijoo.searchia;

import com.opencsv.exceptions.CsvException;
import ir.parsijoo.searchia.config.RankingConfig;
import ir.parsijoo.searchia.config.RankingPhase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static ir.parsijoo.searchia.config.RankingPhaseType.*;
import static ir.parsijoo.searchia.config.SortDirection.ASCENDING;
import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
class RankingExecutorTest {

    List<Doc> docs;
    double totalDuration = 0;
    long maxDuration = 0;

    @BeforeEach
    void setUp() throws IOException {
        docs = TestUtil.createSampleDocs();
    }

    @AfterEach
    void tearDown() {}

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

        List<Doc> result = RankingExecutor.executeRanking(queries, docs, null, rankingConfig, offset, limit);

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
        RankingExecutor.executeRanking(queries, docs, null, rankingConfig, offset, limit);
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

        List<Doc> result = RankingExecutor.executeRanking(queries, docs, null, rankingConfig, offset, limit);

        assertEquals(limit, result.size());
    }

    @Test
    void executeRanking_useRealData_executionTime() throws IOException, CsvException {
        List<Doc> docs = TestUtil.createRealDocs();
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
        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null)
        ));

        long timeThreshold = 20/*ms*/;
        Instant startTime = Instant.now();
        RankingExecutor.executeRanking(queries, docs, null, rankingConfig, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertThat(duration, is(lessThan(timeThreshold)));
    }

    @RepeatedTest(100)
    void executeRanking_useRealData_averageTimeOf100Executions(RepetitionInfo repetitionInfo) throws IOException, CsvException {
        List<Doc> docs = TestUtil.createRealDocs();
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

        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null)
        ));

        double timeThreshold = 50.0/*ms*/;
        Instant startTime = Instant.now();
        RankingExecutor.executeRanking(queries, docs, null, rankingConfig, offset, limit);
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
