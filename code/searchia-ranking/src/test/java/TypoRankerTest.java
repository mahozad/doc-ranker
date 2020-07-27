import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TypoRankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    RankConfiguration configuration;

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
                List.of("viewCount", "creationDate")
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rankByTypo() {
        int typoThreshold = 2;
        List<Doc> result = TypoRanker.rankByTypo(query, docs, typoThreshold);

        assertEquals(1, result.get(0).getId());
    }

    @Test
    void rankDocsByWordTypo() {
        String word = "charter";

        List<Doc> result = TypoRanker.sortDocsByWordTypo(word, docs);

        assertEquals(1, result.get(0).getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void measureWordsDistance_sameWords(int maxTypos) {
        String word1 = "charger";
        String word2 = "charger";

        int distance = TypoRanker.measureWordsDistance(word1, word2, maxTypos);

        assertEquals(0, distance);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void measureWordsDistance_1Typo(int maxTypos) {
        String word1 = "charger";
        String word2 = "charter";

        int distance = TypoRanker.measureWordsDistance(word1, word2, maxTypos);

        assertEquals(1, distance);
    }

    @ParameterizedTest
    @ValueSource(ints = {2})
    void measureWordsDistance_2Typo(int maxTypos) {
        String word1 = "charger";
        String word2 = "sharper";

        int distance = TypoRanker.measureWordsDistance(word1, word2, maxTypos);

        assertEquals(2, distance);
    }
}
