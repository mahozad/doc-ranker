package searchia;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import searchia.*;
import searchia.Query.QueryType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    void rankByTypo_emptyCorrectQueryAndSuggestQuery_resultShouldBe2Groups() {
        Query query1 = new Query("dodge charter", QueryType.ORIGINAL);
        Query query2 = new Query("dodge charter*", QueryType.WILDCARD);
        Query query3 = new Query("dodge challenger", QueryType.OPTIONAL);
        List<Query> queries = List.of(query1, query2, query3);
        DocumentProcessor.processDocs(docs);

        List<Doc> result = TypoRanker.rankByTypo(queries, docs);

        // The set contains two numbers; in other words there is only 2 different scores
        assertEquals(2, result.stream().map(Doc::getPhaseScore).collect(toSet()).size());
    }

    @Test
    void isDocMatchingWithQuery() {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Doc doc = docs.get(1);
        doc.setTokens(Map.of("dodge", new TokenInfo(), "charter", new TokenInfo()));

        boolean isMatching = TypoRanker.isDocMatchingWithQuery(doc, query);

        assertTrue(isMatching);
    }
}
