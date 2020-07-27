import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankerTest {

    String query;
    List<Doc> matches;
    List<Promotion> promotions;

    @BeforeEach
    void setUp() {
        query = "charger";
        matches = List.of(
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2),
                new Doc(2)
        );
        promotions = List.of(
                new Promotion(),
                new Promotion()
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rank() {
        RankConfiguration configuration = new RankConfiguration();

        List<Doc> result = Ranker.rank(query, matches, promotions, configuration, 0, 10);

        assertEquals(1, result.get(0).getId());
    }

    @Test
    void rank_resultSize() {
        RankConfiguration configuration = new RankConfiguration();

        List<Doc> result = Ranker.rank(query, matches, promotions, configuration, 0, 10);

        assertEquals(10, result.size());
    }
}
