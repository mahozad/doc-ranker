package ir.parsijoo.searchia;

import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.parse.QueryParser;
import ir.parsijoo.searchia.parse.RecordParser;
import ir.parsijoo.searchia.rank.PositionRanker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.config.RankingPhaseType.WORDS_POSITION;
import static ir.parsijoo.searchia.config.SortDirection.ASCENDING;
import static java.util.Comparator.comparingInt;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionRankerTest {

    List<Record> records;
    PositionRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
        ranker = new PositionRanker();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void rankByWordPosition() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charger*", QueryType.CORRECTED);
        Query query3 = new Query("red dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.CORRECTED, query2,
                QueryType.SUGGESTED, query3
        );
        QueryParser.parseQueries(queries);
        RecordParser.parseRecords(records);
        RankingPhase phase = new RankingPhase(WORDS_POSITION, true, 0, ASCENDING, null);

        ranker.rank(queries, records, phase);
        records.sort(comparingInt(Record::getRank));

        assertEquals(0, records.stream().filter(record -> record.getId() == 2).findFirst().get().getRank());
        assertEquals(1, records.stream().filter(record -> record.getId() == 6).findFirst().get().getRank());
        assertEquals(1, records.stream().filter(record -> record.getId() == 6).findFirst().get().getMinPosition().value);
        assertEquals("title", records.stream().filter(record -> record.getId() == 6).findFirst().get().getMinPosition().attributeName);
    }

    @Test
    void getRecordMinWordPositionByQuery() throws IOException {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Record record = records.stream().filter(d -> d.getId() == 2).findFirst().get();
        QueryParser.parseQueries(Map.of(QueryType.ORIGINAL, query));
        RecordParser.parseRecord(record);

        int minPosition = PositionRanker.getRecordMinWordPositionByQuery(record, query);

        assertEquals(0, minPosition);
    }

    @Test
    void getRecordMinWordPositionByQuery_recordHasMinPositionInSecondAttribute() throws IOException {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Record record = records.stream().filter(d -> d.getId() == 3).findFirst().get();
        QueryParser.parseQueries(Map.of(QueryType.ORIGINAL, query));
        RecordParser.parseRecord(record);

        int minPosition = PositionRanker.getRecordMinWordPositionByQuery(record, query);

        assertEquals(0, minPosition);
    }

    @Test
    void getRecordMinWordPositionByQuery_aWordIsRepeatedInMultipleAttributesWithDifferentPositions() throws IOException {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Record record = records.stream().filter(d -> d.getId() == 6).findFirst().get();
        QueryParser.parseQueries(Map.of(QueryType.ORIGINAL, query));
        RecordParser.parseRecord(record);

        int minPosition = PositionRanker.getRecordMinWordPositionByQuery(record, query);

        assertEquals(1, minPosition);
    }
}
