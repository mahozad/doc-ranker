package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.TestUtil;
import ir.parsijoo.searchia.model.Record;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class RankUpdaterTest {

    List<Record> records;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void updateRanks() {
        records.stream().filter(record -> record.getId() < 5).forEach(record -> record.setRank(1));
        records.stream().filter(record -> record.getId() % 2 == 0).forEach(record -> record.setNumberOfMatches(1));
        List<Integer> expectedRanks = List.of(3, 2, 3, 2, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1);

        new RankUpdater<>(records, Record::getNumberOfMatches, DESCENDING).updateRanks();

        assertThat(records.stream().map(Record::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }

    @Test // see https://stackoverflow.com/a/3637974
    void updateRanks_ensureComparingBoxedTypesWithEqualValuesProducesCorrectResult() {
        records.stream().filter(record -> record.getId() < 5).forEach(record -> record.setRank(1));
        records.stream().filter(record -> record.getId() % 2 == 0).forEach(record -> record.setNumberOfMatches(1));
        records.stream().filter(record -> record.getId() > 10).forEach(record -> record.setNumberOfMatches(Integer.MAX_VALUE));
        List<Integer> expectedRanks = List.of(4, 3, 4, 3, 2, 1, 2, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0);

        new RankUpdater<>(records, Record::getNumberOfMatches, DESCENDING).updateRanks();

        assertThat(records.stream().map(Record::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }
}
