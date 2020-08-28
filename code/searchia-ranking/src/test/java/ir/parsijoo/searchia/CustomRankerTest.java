package ir.parsijoo.searchia;

import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.parser.RecordParser;
import ir.parsijoo.searchia.ranker.CustomRanker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static ir.parsijoo.searchia.config.RankingPhaseType.CUSTOM;
import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CustomRankerTest {

    List<Record> records;
    CustomRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
        ranker = new CustomRanker();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void rankByCustomAttributes_viewCount() {
        RecordParser.parseRecords(records);
        List<Integer> expectedRanks = List.of(9, 6, 9, 6, 6, 10, 6, 8, 4, 1, 0, 5, 2, 3, 7, 2, 2);
        RankingPhase phase = new RankingPhase(CUSTOM, true, 0, DESCENDING, "viewCount");

        ranker.rank(null, records, phase);

        assertThat(records.stream().map(Record::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }

    @Test
    void rankByCustomAttributes_creationDate() {
        RecordParser.parseRecords(records);
        List<Integer> expectedRanks = List.of(11, 0, 13, 4, 14, 10, 6, 7, 3, 12, 9, 2, 1, 8, 5, 9, 11);
        RankingPhase phase = new RankingPhase(CUSTOM, true, 0, DESCENDING, "creationDate");

        ranker.rank(null, records, phase);

        assertThat(records.stream().map(Record::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }

    @Test
    void rankByCustomAttributes_bothAttributes() {
        RecordParser.parseRecords(records);
        List<Integer> expectedRanks = List.of(14, 8, 15, 9, 11, 16, 10, 13, 6, 1, 0, 7, 2, 5, 12, 3, 4);
        RankingPhase phase1 = new RankingPhase(CUSTOM, true, 0, DESCENDING, "viewCount");
        RankingPhase phase2 = new RankingPhase(CUSTOM, true, 0, DESCENDING, "creationDate");

        ranker.rank(null, records, phase1);
        ranker.rank(null, records, phase2);

        assertThat(records.stream().map(Record::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }
}
