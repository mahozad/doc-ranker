import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rank() {
        String query = "charger";
        List<Doc> matches = List.of(
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc(),
                new Doc()
        );
        List<Promotion> promotions = List.of(
                new Promotion(),
                new Promotion()
        );
        RankConfiguration configuration = new RankConfiguration();

        List<Doc> result = Ranker.rank(query, matches, promotions, configuration, 0, 10);

        assertEquals(1, result.get(0).getId());
    }
}
