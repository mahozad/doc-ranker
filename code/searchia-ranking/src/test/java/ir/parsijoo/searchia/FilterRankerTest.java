package ir.parsijoo.searchia;

import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.rank.FilterRanker;
import ir.parsijoo.searchia.rank.FilterRanker.Filter;
import ir.parsijoo.searchia.rank.FilterRanker.Operator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterRankerTest {

    List<Record> records;
    FilterRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
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
        List<Record> result = FilterRanker.rankByFilters(records, null);

        assertEquals(1, result.get(0).getId());
    }

    @Test
    void setRecordScoreByNumericFilter() {
        Record record = records.get(0);
        record.setFilterableAttrs(Map.of("views", 10.0));
        Filter<Double> filter = new Filter<>();
        filter.setAttributeName("views");
        filter.setOperator(Operator.LT);
        filter.setWeight(1);
        filter.setValue(50.0);

        int score = FilterRanker.setRecordScoreByNumericFilter(record, filter);

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
