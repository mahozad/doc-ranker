package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.processor.DocumentProcessor;
import ir.parsijoo.searchia.processor.QueryProcessor;
import ir.parsijoo.searchia.ranker.PositionRanker;
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

    List<Doc> docs;
    PositionRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        docs = TestUtil.createSampleDocs();
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
        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDocs(docs);
        RankingPhase phase = new RankingPhase(WORDS_POSITION, true, 0, ASCENDING, null);

        ranker.rank(queries, docs, phase);
        docs.sort(comparingInt(Doc::getRank));

        assertEquals(0, docs.stream().filter(doc -> doc.getId() == 2).findFirst().get().getRank());
        assertEquals(1, docs.stream().filter(doc -> doc.getId() == 6).findFirst().get().getRank());
        assertEquals(1, docs.stream().filter(doc -> doc.getId() == 6).findFirst().get().getMinPosition().value);
        assertEquals("title", docs.stream().filter(doc -> doc.getId() == 6).findFirst().get().getMinPosition().attributeName);
    }

    @Test
    void getDocMinWordPositionByQuery() throws IOException {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Doc doc = docs.stream().filter(d -> d.getId() == 2).findFirst().get();
        QueryProcessor.processQueries(Map.of(QueryType.ORIGINAL, query));
        DocumentProcessor.processDoc(doc);

        int minPosition = PositionRanker.getDocMinWordPositionByQuery(doc, query);

        assertEquals(0, minPosition);
    }

    @Test
    void getDocMinWordPositionByQuery_docHasMinPositionInSecondAttribute() throws IOException {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Doc doc = docs.stream().filter(d -> d.getId() == 3).findFirst().get();
        QueryProcessor.processQueries(Map.of(QueryType.ORIGINAL, query));
        DocumentProcessor.processDoc(doc);

        int minPosition = PositionRanker.getDocMinWordPositionByQuery(doc, query);

        assertEquals(0, minPosition);
    }

    @Test
    void getDocMinWordPositionByQuery_aWordIsRepeatedInMultipleAttributesWithDifferentPositions() throws IOException {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Doc doc = docs.stream().filter(d -> d.getId() == 6).findFirst().get();
        QueryProcessor.processQueries(Map.of(QueryType.ORIGINAL, query));
        DocumentProcessor.processDoc(doc);

        int minPosition = PositionRanker.getDocMinWordPositionByQuery(doc, query);

        assertEquals(1, minPosition);
    }
}
