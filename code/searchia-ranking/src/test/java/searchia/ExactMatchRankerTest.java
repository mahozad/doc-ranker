package searchia;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchia.Query.QueryType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ExactMatchRankerTest {

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
    void rankByExactMatch() {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        @SuppressWarnings("SpellCheckingInspection")
        Query query2 = new Query("aventado*", QueryType.WILDCARD);
        Map<QueryType, Query> queries = Map.of(
                QueryType.ORIGINAL, query1,
                QueryType.WILDCARD, query2
        );
        DocumentProcessor.processDocs(docs);

        List<Doc> result = ExactMatchRanker.rankByExactMatch(queries, docs);

        assertTrue(result.stream().filter(doc -> doc.getId() == 8).findFirst().get().getRank() == 1);
        assertTrue(result.stream().filter(doc -> doc.getRank() == 1).count() == 1);
        assertTrue(result.stream().filter(doc -> doc.getRank() == 0).count() == result.size() - 1);
    }
}
