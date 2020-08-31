package ir.parsijoo.searchia;

import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.parser.QueryParser;
import ir.parsijoo.searchia.parser.RecordParser;
import ir.parsijoo.searchia.ranker.OptionalWordRanker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.config.RankingPhaseType.OPTIONAL_WORDS;
import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalWordRankerTest {

    List<Record> records;
    OptionalWordRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
        ranker = new OptionalWordRanker();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void rankByOptionalWords_noOptionalQuery() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge red charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
        );
        QueryParser.parseQueries(queries);
        RecordParser.parseRecords(records);
        RankingPhase phase = new RankingPhase(OPTIONAL_WORDS, true, 0, DESCENDING, null);

        ranker.rank(queries, records, phase);

        // The set contains one number; in other words all the records have the same numberOfMatches
        assertEquals(1, records.stream().map(Record::getNumberOfMatches).collect(toSet()).size());
    }

    @Test
    void rankByOptionalWords_withOptionalQuery() throws IOException {
        Query query1 = new Query("dodge charger", QueryType.ORIGINAL);
        Query query2 = new Query("dodge challenger", QueryType.SUGGESTED);
        Query query3 = new Query("charger", QueryType.OPTIONAL);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.SUGGESTED, query2,
                QueryType.OPTIONAL, query3
        );
        QueryParser.parseQueries(queries);
        RecordParser.parseRecords(records);
        Set<Integer> expectedNonOptionalMatchIds = Set.of(1, 2, 3, 7, 9, 10, 11, 12, 16, 17);
        RankingPhase phase = new RankingPhase(OPTIONAL_WORDS, true, 0, DESCENDING, null);

        ranker.rank(queries, records, phase);

        records.sort(comparingInt(Record::getRank));
        List<Record> nonOptionalMatches = records.subList(0, expectedNonOptionalMatchIds.size());
        assertTrue(nonOptionalMatches.stream().map(Record::getId).collect(toSet()).containsAll(expectedNonOptionalMatchIds));
    }
}
