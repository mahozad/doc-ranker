import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    RankConfiguration configuration;

    // @formatter:off
    @BeforeEach
    void setUp() throws IOException {
        query = "charger";

        docs = Files
                .lines(samplesPath)
                .skip(1)
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    long creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    long viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    double score = Math.random();
                    Attribute<String> title = new Attribute<>(attrs[3].split("=")[0], attrs[3].split("=")[0]);
                    Attribute<String> description = new Attribute<>(attrs[4].split("=")[0], attrs[4].split("=")[0]);
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
                Set.of()
        );
    }
    // @formatter:on

    @AfterEach
    void tearDown() {
    }

    @Test
    void sortDocs() {
        docs.get(0).setPhaseScore(5);
        docs.get(2).setPhaseScore(3);
        docs.get(10).setPhaseScore(7);
        docs.get(12).setPhaseScore(4);

        docs = docs.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        assertEquals(7, docs.get(0).getPhaseScore());
        assertEquals(5, docs.get(1).getPhaseScore());
        assertEquals(4, docs.get(2).getPhaseScore());
        assertEquals(3, docs.get(3).getPhaseScore());
        assertEquals(0, docs.get(4).getPhaseScore());
    }

    @Test
    void rank() {
        List<Doc> result = Ranker.rank(query, docs, promotions, configuration, 0, 10);

        assertEquals(1, result.get(0).getId());
    }

    @Test
    void rank_time() {
        long timeThreshold = 200/*ms*/;

        Instant startTime = Instant.now();
        Ranker.rank(query, docs, promotions, configuration, 0, 10);
        long duration = Duration.between(startTime, Instant.now()).toMillis();

        assertTrue(() -> duration < timeThreshold);
    }

    @Test
    void rank_resultSize() {
        List<Doc> result = Ranker.rank(query, docs, promotions, configuration, 0, 10);

        assertEquals(10, result.size());
    }
}
