package ir.parsijoo.searchia;

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
import java.util.SortedMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

class OptionalWordRankerTest {

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
    void rankByOptionalWords_noOptionalQuery() throws IOException {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge red charger", QueryType.SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2,
                QueryType.SUGGESTED, query3
        );
        DocumentProcessor.processDocs(docs);

        List<Doc> result = OptionalWordRanker.rankByOptionalWords(queries, docs);

        // The set contains one number; in other words all the docs have the same numberOfMatches
        assertEquals(1, result.stream().map(Doc::getNumberOfMatches).collect(toSet()).size());
    }

    @Test
    void groupDocsByRank() {
        // Set rank of two docs to size - 2 and the rest have default rank of 0 (so two rank groups)
        docs.get(1).setRank(docs.size() - 2);
        docs.get(11).setRank(docs.size() - 2);
        Set<Integer> expectedGroupSizes = Set.of(2, docs.size() - 2);

        SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
        Set<Integer> groupSizes = groups.values().stream().map(List::size).collect(toSet());

        assertTrue(groupSizes.containsAll(expectedGroupSizes));
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
        DocumentProcessor.processDocs(docs);
        Set<Integer> expectedNonOptionalMatchIds = Set.of(1, 2, 3, 7, 9, 10, 11, 12, 16, 17);

        List<Doc> result = OptionalWordRanker.rankByOptionalWords(queries, docs);
        result.sort((o1, o2) -> (int) (o1.getRank() - o2.getRank()));
        List<Doc> nonOptionalMatches = result.subList(0, expectedNonOptionalMatchIds.size());

        assertTrue(nonOptionalMatches.stream().map(Doc::getId).collect(toSet()).containsAll(expectedNonOptionalMatchIds));
    }
}
