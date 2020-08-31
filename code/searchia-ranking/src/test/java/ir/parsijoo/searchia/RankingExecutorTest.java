package ir.parsijoo.searchia;

import com.opencsv.exceptions.CsvException;
import ir.parsijoo.searchia.config.RankingConfig;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.config.RankingPhaseType.*;
import static ir.parsijoo.searchia.config.SortDirection.ASCENDING;
import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RankingExecutorTest {

    List<Record> records;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void sortRecords() {
        records.get(0).setRank(5);
        records.get(2).setRank(3);
        records.get(10).setRank(7);
        records.get(12).setRank(4);

        records = records.stream().sorted().collect(toList());

        assertEquals(7, records.get(records.size()-1).getRank());
        assertEquals(5, records.get(records.size()-2).getRank());
        assertEquals(4, records.get(records.size()-3).getRank());
        assertEquals(3, records.get(records.size()-4).getRank());
        assertEquals(1, records.subList(0, records.size()-5).stream().map(Record::getRank).collect(toSet()).size());
    }

    @Test
    void executeRanking() throws IOException {
        int offset = 0;
        int limit = 10;
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
        );
        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null),
                new RankingPhase(CUSTOM, true, 5, DESCENDING, "viewCount")
        ));
        List<Integer> expectedRecordIdOrder =
                List.of(17, 2, 16, 10, 7, 1, 9, 12, 3, 11, 14, 15, 6, 5, 8, 13, 4).subList(offset, offset + limit);

        List<Record> result = RankingExecutor.executeRanking(queries, records, null, rankingConfig, offset, limit);

        assertThat(result.stream().map(Record::getId).collect(toList()), is(equalTo(expectedRecordIdOrder)));
    }

    @Test
    void executeRanking_resultSize() throws IOException {
        int offset = 0;
        int limit = 10;
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
        );
        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null),
                new RankingPhase(CUSTOM, true, 5, DESCENDING, "viewCount")
        ));

        List<Record> result = RankingExecutor.executeRanking(queries, records, null, rankingConfig, offset, limit);

        assertEquals(limit, result.size());
    }

    @Test
    void executeRanking_executionTime() throws IOException {
        long timeThreshold = 50/*ms*/;
        int offset = 0;
        int limit = 10;
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
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
        RankingExecutor.executeRanking(queries, records, null, rankingConfig, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertThat(duration, is(lessThan(timeThreshold)));
    }

    @Test
    void executeRanking_useRealData_executionTime() throws IOException, CsvException {
        List<Record> records = TestUtil.createRealRecords();
        int offset = 0;
        int limit = 10;
        Query query1 = new Query("معرفی فیل", QueryType.ORIGINAL);
        Query query2 = new Query("معرفی فیل*", QueryType.WILDCARD);
        Query query3 = new Query("معرفی فیلم", QueryType.CORRECTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.CORRECTED, query3
        );
        RankingConfig rankingConfig = new RankingConfig(Set.of(
                new RankingPhase(TYPO, true, 0, ASCENDING, null),
                new RankingPhase(OPTIONAL_WORDS, true, 1, DESCENDING, null),
                new RankingPhase(WORDS_DISTANCE, true, 2, ASCENDING, null),
                new RankingPhase(WORDS_POSITION, true, 3, ASCENDING, null),
                new RankingPhase(EXACT_MATCH, true, 4, DESCENDING, null),
                new RankingPhase(CUSTOM, true, 5, DESCENDING, "score"),
                new RankingPhase(CUSTOM, true, 6, DESCENDING, "clicks")
        ));

        long timeThreshold = 20/*ms*/;
        Instant startTime = Instant.now();
        RankingExecutor.executeRanking(queries, records, null, rankingConfig, offset, limit);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertThat(duration, is(lessThan(timeThreshold)));
    }
}
