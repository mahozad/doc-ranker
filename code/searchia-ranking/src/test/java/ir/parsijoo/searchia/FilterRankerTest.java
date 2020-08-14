package ir.parsijoo.searchia;

import ir.parsijoo.searchia.ranker.FilterRanker;
import ir.parsijoo.searchia.ranker.FilterRanker.Filter;
import ir.parsijoo.searchia.ranker.FilterRanker.Operator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterRankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;

    @BeforeEach
    void setUp() throws IOException {
        query = "dodge charger";

        docs = Files
                .lines(samplesPath)
                .filter(line -> !line.startsWith("#"))
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    double score = Math.random();
                    long creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    long viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    String title = attrs[3].split("=")[1];
                    String description = attrs[4].split("=")[1];
                    Map<String, String> searchableAttrs = Map.of("title", title, "description", description);
                    Map<String, Long> customAttrs = Map.of("viewCount", viewCount, "creationDate", creationDate);
                    return new Doc(id, customAttrs, score, searchableAttrs);
                })
                .collect(Collectors.toList());

        promotions = List.of(
                new Promotion(),
                new Promotion()
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rankByFilters_1filter() {
        Filter<Double> filter = new Filter<>();
        filter.setValue(50.0);
        filter.setAttributeName("views");
        filter.setOperator(Operator.LT);
        List<Doc> result = FilterRanker.rankByFilters(docs, null);

        assertEquals(1, result.get(0).getId());
    }

    @Test
    void setDocScoreByNumericFilter() {
        Doc doc = docs.get(0);
        doc.setFilterableAttrs(Map.of("views", 10.0));
        Filter<Double> filter = new Filter<>();
        filter.setAttributeName("views");
        filter.setOperator(Operator.LT);
        filter.setWeight(1);
        filter.setValue(50.0);

        int score = FilterRanker.setDocScoreByNumericFilter(doc, filter);

        assertEquals(1, score);
    }

    @Test
    void isFilterSatisfied_numericFilter() {
        Filter<Double> filter = new Filter<>();
        filter.setOperator(Operator.LT);
        filter.setValue(50.0);

        boolean satisfied = filter.isFilterSatisfied(40.0);

        assertTrue(satisfied);
    }

    @Test
    void isFilterSatisfied_textFilter() {
        Filter<String> filter = new Filter<>();
        filter.setOperator(Operator.EQ);
        filter.setValue("new");

        boolean satisfied = filter.isFilterSatisfied("new");

        assertTrue(satisfied);
    }
}
