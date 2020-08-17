//package ir.parsijoo.searchia;
//
//import ir.parsijoo.searchia.Query.QueryType;
//import ir.parsijoo.searchia.config.RankingPhase;
//import ir.parsijoo.searchia.processor.QueryProcessor;
//import ir.parsijoo.searchia.processor.RecordProcessor;
//import ir.parsijoo.searchia.ranker.TypoRanker;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import static ir.parsijoo.searchia.config.RankingPhaseType.TYPO;
//import static ir.parsijoo.searchia.config.SortDirection.ASCENDING;
//import static java.util.stream.Collectors.toList;
//import static java.util.stream.Collectors.toSet;
//import static org.junit.jupiter.api.Assertions.*;
//
//class TypoRankerTest {
//
//    List<Record> records;
//    TypoRanker ranker;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        records = TestUtil.createSampleRecords();
//        ranker = new TypoRanker();
//    }
//
//    @AfterEach
//    void tearDown() {}
//
//    // @Test
//    // void rankByTypo_allQueryTypes() {
//    //     Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
//    //     Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
//    //     Query query3 = new Query("dodge charger", QueryType.CORRECTED);
//    //     Query query4 = new Query("dodge challenger", QueryType.SUGGESTED);
//    //     Query query5 = new Query("dodge challenger", QueryType.OPTIONAL);
//    //     List<Query> queries = List.of(query1, query2, query3, query4, query5);
//    //
//    //     List<Record> result = TypoRanker.rankByTypo(queries, records);
//    // }
//
//    @Test
//    void rankByTypo_emptyCorrectQueryAndSuggestQuery_resultShouldBe1Group() throws IOException {
//        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
//        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
//        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
//        Map<QueryType, Query> queries = Map.of(
//                QueryType.ORIGINAL, query1,
//                QueryType.WILDCARD, query2,
//                QueryType.OPTIONAL, query3
//        );
//        QueryProcessor.processQueries(queries);
//        RecordProcessor.processRecords(records);
//        RankingPhase phase = new RankingPhase(TYPO, true, 0, ASCENDING, null);
//
//        ranker.rank(queries, records, phase);
//
//        // The set contains one number; in other words all the records have the same rank
//        assertEquals(1, records.stream().map(Record::getRank).collect(toSet()).size());
//    }
//
//    @Test
//    void rankByTypo_emptyCorrectQueryAndSuggestQuery_allRecordsShouldHaveNoTypos() throws IOException {
//        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
//        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
//        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
//        Map<QueryType, Query> queries = Map.of(
//                QueryType.ORIGINAL, query1,
//                QueryType.WILDCARD, query2,
//                QueryType.OPTIONAL, query3
//        );
//        QueryProcessor.processQueries(queries);
//        RecordProcessor.processRecords(records);
//        RankingPhase phase = new RankingPhase(TYPO, true, 0, ASCENDING, null);
//
//        ranker.rank(queries, records, phase);
//
//        // The set contains one number; in other words all the records have the same number of typo
//        assertEquals(1, records.stream().map(Record::getNumberOfTypos).collect(toSet()).size());
//        assertEquals(0, records.stream().map(Record::getNumberOfTypos).collect(toList()).get(0));
//    }
//
//    @Test
//    void rankByTypo_emptyCorrectQueryAndSuggestQuery_topResultsShouldMatchOriginalOrWildcard() throws IOException {
//        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
//        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
//        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
//        Map<QueryType, Query> queries = Map.of(
//                QueryType.ORIGINAL, query1,
//                QueryType.WILDCARD, query2,
//                QueryType.OPTIONAL, query3
//        );
//        QueryProcessor.processQueries(queries);
//        RecordProcessor.processRecords(records);
//        Set<Integer> expectedIds = Set.of(2, 16, 17);
//        RankingPhase phase = new RankingPhase(TYPO, true, 0, ASCENDING, null);
//
//        ranker.rank(queries, records, phase);
//
//        List<Integer> resultIds = records.stream().map(Record::getId).collect(toList());
//        assertTrue(resultIds.subList(0, 3).containsAll(expectedIds));
//    }
//
//    @Test
//    void rankByTypo_containsCorrectQueryOrSuggestQuery_resultShouldBe2Groups() throws IOException {
//        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
//        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
//        Query query3 = new Query("dodge charger", QueryType.SUGGESTED);
//        Map<QueryType, Query> queries = Map.of(
//                QueryType.ORIGINAL, query1,
//                QueryType.WILDCARD, query2,
//                QueryType.SUGGESTED, query3
//        );
//        QueryProcessor.processQueries(queries);
//        RecordProcessor.processRecords(records);
//        RankingPhase phase = new RankingPhase(TYPO, true, 0, ASCENDING, null);
//
//        ranker.rank(queries, records, phase);
//
//        // The set contains two numbers; in other words there are 2 different scores
//        assertEquals(2, records.stream().map(Record::getRank).collect(toSet()).size());
//    }
//
//    @Test
//    void rankByTypo_containsCorrectQueryOrSuggestQuery_topResultsShouldMatchOriginalOrWildcard() throws IOException {
//        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
//        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
//        Query query3 = new Query("dodge charger", QueryType.SUGGESTED);
//        Map<QueryType, Query> queries = Map.of(
//                QueryType.ORIGINAL, query1,
//                QueryType.WILDCARD, query2,
//                QueryType.SUGGESTED, query3
//        );
//        QueryProcessor.processQueries(queries);
//        RecordProcessor.processRecords(records);
//        RankingPhase phase = new RankingPhase(TYPO, true, 0, ASCENDING, null);
//
//        ranker.rank(queries, records, phase);
//
//        records = records.stream().sorted().collect(toList());
//        Set<Integer> group1Ranks = records.subList(0, 3).stream().map(Record::getRank).collect(toSet());
//        Set<Integer> group2Ranks = records.subList(3, records.size()).stream().map(Record::getRank).collect(toSet());
//        assertEquals(1, group1Ranks.size());
//        assertEquals(1, group2Ranks.size());
//    }
//
//    @Test
//    void queriesContainCorrectedOrSuggested() {
//        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
//        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
//        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
//        Map<QueryType, Query> queries = Map.of(
//                QueryType.ORIGINAL, query1,
//                QueryType.WILDCARD, query2,
//                QueryType.OPTIONAL, query3
//        );
//
//        boolean containsCorrectedOrSuggested = TypoRanker.queriesContainCorrectedOrSuggested(queries);
//
//        assertFalse(containsCorrectedOrSuggested);
//    }
//}
