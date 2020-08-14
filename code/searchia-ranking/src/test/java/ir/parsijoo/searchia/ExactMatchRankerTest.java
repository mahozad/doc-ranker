package ir.parsijoo.searchia;


import ir.parsijoo.searchia.Query.QueryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ExactMatchRankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    RankConfiguration configuration;
    ExactMatchRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        query = "dodge charger";

        docs = Files
                .lines(samplesPath)
                .filter(line -> !line.startsWith("#"))
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    long creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    long viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    double score = Math.random();
                    Attribute<String> title = new Attribute<>(attrs[3].split("=")[0], attrs[3].split("=")[1]);
                    Attribute<String> description = new Attribute<>(attrs[4].split("=")[0], attrs[4].split("=")[1]);
                    List<Attribute<String>> searchableAttrs = List.of(title, description);
                    Map<String, Long> customAttrs = Map.of("viewCount", viewCount, "creationDate", creationDate);
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
                Set.of("dodge")
        );

        ranker = new ExactMatchRanker();
    }

    @AfterEach
    void tearDown() {
    }

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

        ranker.rank(queries, docs, configuration);

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

        ranker.rank(queries, docs, configuration);

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

        ranker.rank(queries, docs, configuration);

        assertThat(docs.stream().filter(doc -> doc.getRank() == 0).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup1Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 1).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup2Ids)));
        assertThat(docs.stream().filter(doc -> doc.getRank() == 2).map(Doc::getId).collect(Collectors.toSet()), is(equalTo(expectedGroup3Ids)));
    }
}
