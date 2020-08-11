package ir.parsijoo.searchia;


import ir.parsijoo.searchia.Doc.MinDistance;
import ir.parsijoo.searchia.Query.QueryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceRankerTest {

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
    }

    @AfterEach
    void tearDown() {
    }

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
        DocumentProcessor.processDocs(docs);

        DistanceRanker.rankByWordsDistance(queries, docs);

        docs.sort(comparingInt(Doc::getRank));
        assertTrue(docs.get(0).getId() == 2 && docs.get(0).getRank() == 0);
        assertTrue(docs.get(1).getId() == 3 && docs.get(1).getRank() == 1);
        assertTrue(docs.get(2).getId() == 16 && docs.get(2).getRank() == 2);
        assertEquals(3, docs.get(10).getRank());
    }

    @Test
    void getDocMinDistanceFromQueries() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge red charger", QueryType.CORRECTED);
        Query query3 = new Query("red dodge charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.CORRECTED, query2,
                QueryType.SUGGESTED, query3
        );
        Doc doc = docs.stream().filter(d -> d.getId() == 2).findFirst().get();
        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDoc(doc);

        MinDistance minDistance = DistanceRanker.getDocMinDistanceFromQueries(doc, queries);

        assertEquals(2, minDistance.value);
        assertEquals(QueryType.CORRECTED, minDistance.query);
    }

    @Test
    void calculateDocDistanceFromQuery() throws IOException {
        Query query = new Query("red dodge charger", QueryType.ORIGINAL);
        Doc doc = docs.stream().filter(d -> d.getId() == 2).findFirst().get();
        QueryProcessor.processQueries(Map.of(QueryType.ORIGINAL, query));
        DocumentProcessor.processDoc(doc);

        int distance = DistanceRanker.calculateDocDistanceFromQuery(doc, query);

        assertEquals(4, distance);
    }

    @Test
    void calculateDocDistanceFromQuery_docLacksOneOfQueryWords() throws IOException {
        Query query = new Query("red dodge charger", QueryType.ORIGINAL);
        Doc doc = docs.stream().filter(d -> d.getId() == 1).findFirst().get();
        QueryProcessor.processQueries(Map.of(QueryType.ORIGINAL, query));
        DocumentProcessor.processDoc(doc);

        int distance = DistanceRanker.calculateDocDistanceFromQuery(doc, query);

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
