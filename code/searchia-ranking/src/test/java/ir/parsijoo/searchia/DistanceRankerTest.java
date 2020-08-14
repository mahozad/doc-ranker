package ir.parsijoo.searchia;


import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.Record.MinDistance;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.processor.QueryProcessor;
import ir.parsijoo.searchia.processor.RecordProcessor;
import ir.parsijoo.searchia.ranker.DistanceRanker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.config.RankingPhaseType.WORDS_DISTANCE;
import static ir.parsijoo.searchia.config.SortDirection.ASCENDING;
import static java.util.Comparator.comparingInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceRankerTest {

    List<Record> records;
    DistanceRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
        ranker = new DistanceRanker();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void rankByWordsDistance() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("red dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
        );
        QueryProcessor.processQueries(queries);
        RecordProcessor.processRecords(records);
        RankingPhase phase = new RankingPhase(WORDS_DISTANCE, true, 0, ASCENDING, null);

        ranker.rank(queries, records, phase);

        records.sort(comparingInt(Record::getRank));
        assertTrue(records.get(0).getId() == 2 && records.get(0).getRank() == 0);
        assertTrue(records.get(1).getId() == 3 && records.get(1).getRank() == 1);
        assertTrue(records.get(2).getId() == 16 && records.get(2).getRank() == 2);
        assertEquals(3, records.get(10).getRank());
    }

    @Test
    void getRecordMinDistanceFromQueries() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge red charger", QueryType.CORRECTED);
        Query query3 = new Query("red dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.CORRECTED, query2,
                QueryType.SUGGESTED, query3
        );
        Record record = records.stream().filter(d -> d.getId() == 2).findFirst().get();
        QueryProcessor.processQueries(queries);
        RecordProcessor.processRecord(record);

        MinDistance minDistance = DistanceRanker.getRecordMinDistanceFromQueries(record, queries);

        assertEquals(2, minDistance.value);
        assertEquals(QueryType.CORRECTED, minDistance.query);
    }

    @Test
    void calculateRecordDistanceFromQuery() throws IOException {
        Query query = new Query("red dodge charger", QueryType.ORIGINAL);
        Record record = records.stream().filter(d -> d.getId() == 2).findFirst().get();
        QueryProcessor.processQueries(Map.of(QueryType.ORIGINAL, query));
        RecordProcessor.processRecord(record);

        int distance = DistanceRanker.calculateRecordDistanceFromQuery(record, query);

        assertEquals(4, distance);
    }

    @Test
    void calculateRecordDistanceFromQuery_recordLacksOneOfQueryWords() throws IOException {
        Query query = new Query("red dodge charger", QueryType.ORIGINAL);
        Record record = records.stream().filter(d -> d.getId() == 1).findFirst().get();
        QueryProcessor.processQueries(Map.of(QueryType.ORIGINAL, query));
        RecordProcessor.processRecord(record);

        int distance = DistanceRanker.calculateRecordDistanceFromQuery(record, query);

        assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists() {
        List<Integer> positions1 = List.of(1, 2);
        List<Integer> positions2 = List.of(3, 5, 6);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(1, minDistance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists_minDistanceIsInSecondAttribute() {
        List<Integer> positions1 = List.of(2, 1_000_000);
        List<Integer> positions2 = List.of(5, 1_000_001, 2_000_000);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(1, minDistance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists_secondWordComesBeforeFirstWord() {
        List<Integer> positions1 = List.of(14, 15, 30, 31);
        List<Integer> positions2 = List.of(3, 12, 20);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(3, minDistance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists_wordsAreInDifferentAttributes() {
        List<Integer> positions1 = List.of(4, 15, 30, 31);
        List<Integer> positions2 = List.of(1_000_013, 1_000_014, 1_000_017, 2_000_016);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(8, minDistance);
    }

    @Test
    void rankByWordsDistance_oneWordQuery() {
    }
}
