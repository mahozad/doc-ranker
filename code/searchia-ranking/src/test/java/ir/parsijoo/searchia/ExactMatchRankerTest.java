package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.processor.DocumentProcessor;
import ir.parsijoo.searchia.processor.QueryProcessor;
import ir.parsijoo.searchia.ranker.ExactMatchRanker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.config.RankingPhaseType.EXACT_MATCH;
import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ExactMatchRankerTest {

    List<Doc> docs;
    ExactMatchRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        docs = TestUtil.createSampleDocs();
        ranker = new ExactMatchRanker();
    }

    @AfterEach
    void tearDown() {}

    @RepeatedTest(5) // Order of reading queries in rankByExactMatch may differ in each execution
    void rankByExactMatch() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        @SuppressWarnings("SpellCheckingInspection")
        Query query2 = new Query("lamborghini aventado*", QueryType.WILDCARD);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2
        );
        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDocs(docs);
        Set<Integer> expectedGroup1Ids = Set.of(2, 16);
        Set<Integer> expectedGroup2Ids = Set.of(1, 3, 6, 7, 8, 9, 10, 11, 12, 17);
        Set<Integer> expectedGroup3Ids = Set.of(4, 5, 13, 14, 15);
        RankingPhase phase = new RankingPhase(EXACT_MATCH, true, 0, DESCENDING, null);

        ranker.rank(queries, docs, phase);

        assertThat(docs.stream().filter(doc -> doc.getRank() == 0).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup1Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 1).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup2Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 2).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup3Ids)));
    }

    @Test
    void rankByExactMatch_queriesContainUnrelatedQueryTypes() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        @SuppressWarnings("SpellCheckingInspection")
        Query query2 = new Query("lamborghini aventado*", QueryType.WILDCARD);
        Query unusedQuery = new Query("dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, unusedQuery
        );
        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDocs(docs);
        Set<Integer> expectedGroup1Ids = Set.of(2, 16);
        Set<Integer> expectedGroup2Ids = Set.of(1, 3, 6, 7, 8, 9, 10, 11, 12, 17);
        Set<Integer> expectedGroup3Ids = Set.of(4, 5, 13, 14, 15);
        RankingPhase phase = new RankingPhase(EXACT_MATCH, true, 0, DESCENDING, null);

        ranker.rank(queries, docs, phase);

        assertThat(docs.stream().filter(doc -> doc.getRank() == 0).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup1Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 1).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup2Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 2).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup3Ids)));
    }

    @Test
    void rankByExactMatch_otherQueryIsLongerThanOriginalQuery_resultShouldBeLengthOfOriginalQuery() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("red new charger", QueryType.EQUIVALENT);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.EQUIVALENT, query2
        );
        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDocs(docs);
        Set<Integer> expectedGroup1Ids = Set.of(2, 3, 16);
        Set<Integer> expectedGroup2Ids = Set.of(1, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 17);
        Set<Integer> expectedGroup3Ids = Set.of(4, 13);
        RankingPhase phase = new RankingPhase(EXACT_MATCH, true, 0, DESCENDING, null);

        ranker.rank(queries, docs, phase);

        assertThat(docs.stream().filter(doc -> doc.getRank() == 0).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup1Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 1).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup2Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 2).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup3Ids)));
    }
}
