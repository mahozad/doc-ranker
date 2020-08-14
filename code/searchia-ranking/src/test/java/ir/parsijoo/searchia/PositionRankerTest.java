package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.dto.RankingPhaseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.dto.RankingPhaseType.WORDS_POSITION;
import static ir.parsijoo.searchia.dto.SortDirection.ASCENDING;
import static java.util.Comparator.comparingInt;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionRankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    PositionRanker ranker;

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

        ranker = new PositionRanker();
    }

    @AfterEach
    void tearDown() {
    }

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
        RankingPhaseDTO phase = new RankingPhaseDTO(WORDS_POSITION, true, 0, ASCENDING, null);

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
