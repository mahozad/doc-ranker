package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

class TypoRankerTest {

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
                .skip(1)
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    long creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    long viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    double score = Math.random();
                    Attribute<String> title = new Attribute<>(attrs[3].split("=")[0],
                            attrs[3].split("=")[1]);
                    Attribute<String> description = new Attribute<>(attrs[4].split("=")[0],
                            attrs[4].split("=")[1]);
                    List<Attribute<String>> searchableAttrs = List.of(title, description);
                    Map<String, Long> customAttrs = Map.of("viewCount", viewCount, "creationDate"
                            , creationDate);
                    return new Doc(id, customAttrs, score, searchableAttrs);
                })
                .collect(Collectors.toList());

        promotions = List.of(
                new Promotion(),
                new Promotion()
        );

        configuration = new RankConfiguration(
                "price",
                null,
                false,
                List.of("viewCount", "creationDate"),
                Set.of()
        );
    }

    @AfterEach
    void tearDown() {
    }

    // @Test
    // void rankByTypo_allQueryTypes() {
    //     Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
    //     Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
    //     Query query3 = new Query("dodge charger", QueryType.CORRECTED);
    //     Query query4 = new Query("dodge challenger", QueryType.SUGGESTED);
    //     Query query5 = new Query("dodge challenger", QueryType.OPTIONAL);
    //     List<Query> queries = List.of(query1, query2, query3, query4, query5);
    //
    //     List<Doc> result = TypoRanker.rankByTypo(queries, docs);
    // }

    @Test
    void rankByTypo_emptyCorrectQueryAndSuggestQuery_resultShouldBe1Group() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.OPTIONAL, query3
        );
        DocumentProcessor.processDocs(docs);

        List<Doc> result = TypoRanker.rankByTypo(queries, docs);

        // The set contains one number; in other words all the docs have the same rank
        assertEquals(1, result.stream().map(Doc::getRank).collect(toSet()).size());
    }

    @Test
    void rankByTypo_emptyCorrectQueryAndSuggestQuery_topResultsShouldMatchOriginalOrWildcard() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.OPTIONAL, query3
        );
        DocumentProcessor.processDocs(docs);
        Set<Integer> expectedIds = Set.of(2, 16, 17);

        List<Doc> result = TypoRanker.rankByTypo(queries, docs);
        List<Integer> resultIds = result.stream().sorted(Comparator.reverseOrder()).map(Doc::getId).collect(toList());

        assertTrue(resultIds.subList(0, 3).containsAll(expectedIds));
    }

    @Test
    void rankByTypo_containsCorrectQueryOrSuggestQuery_resultShouldBe2Groups() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
        );
        DocumentProcessor.processDocs(docs);

        List<Doc> result = TypoRanker.rankByTypo(queries, docs);

        // The set contains two numbers; in other words there are 2 different scores
        assertEquals(2, result.stream().map(Doc::getRank).collect(toSet()).size());
    }

    @Test
    void rankByTypo_containsCorrectQueryOrSuggestQuery_topResultsShouldMatchOriginalOrWildcard() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
        );
        DocumentProcessor.processDocs(docs);

        List<Doc> result = TypoRanker.rankByTypo(queries, docs).stream().sorted(Comparator.reverseOrder()).collect(toList());
        Set<Long> group1Ranks = result.subList(0, 3).stream().map(Doc::getRank).collect(toSet());
        Set<Long> group2Ranks = result.subList(3, result.size()).stream().map(Doc::getRank).collect(toSet());

        assertEquals(1, group1Ranks.size());
        assertEquals(1, group2Ranks.size());
    }

    @Test
    void queriesContainCorrectedOrSuggested() {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.OPTIONAL, query3
        );

        boolean containsCorrectedOrSuggested = TypoRanker.queriesContainCorrectedOrSuggested(queries);

        assertFalse(containsCorrectedOrSuggested);
    }

    @Test
    void isDocMatchingWithQuery() {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Doc doc = docs.get(1);
        doc.setTokens(Map.of("dodge", new TokenInfo(), "charter", new TokenInfo()));

        boolean isMatching = TypoRanker.isDocMatchedWithQuery(doc, query);

        assertTrue(isMatching);
    }

    @Test
    void isDocMatchingWithQuery_wildcardQuery() {
        Query query = new Query("dodge charter*", QueryType.WILDCARD);
        Doc doc = docs.get(1);
        doc.setTokens(Map.of("dodge", new TokenInfo(), "charter", new TokenInfo()));

        boolean isMatching = TypoRanker.isDocMatchedWithQuery(doc, query);

        assertTrue(isMatching);
    }

    @Test
    void isDocMatchingWithQuery_wildcardQuery_withoutAsteriskAtEnd() {
        Query query = new Query("dodge charter", QueryType.WILDCARD);
        Doc doc = docs.get(1);
        doc.setTokens(Map.of("dodge", new TokenInfo(), "charter", new TokenInfo()));

        boolean isMatching = TypoRanker.isDocMatchedWithQuery(doc, query);

        assertTrue(isMatching);
    }
}
