package ir.parsijoo.searchia;

import ir.parsijoo.searchia.ranker.FilterRanker;
import ir.parsijoo.searchia.ranker.FilterRanker.Filter;
import ir.parsijoo.searchia.ranker.FilterRanker.Operator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterRankerTest {

    List<Doc> docs;
    FilterRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        docs = TestUtil.createSampleDocs();
        ranker = new FilterRanker();
    }

    @AfterEach
    void tearDown() {}

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
