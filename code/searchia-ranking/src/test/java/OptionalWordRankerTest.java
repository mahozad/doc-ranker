import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class OptionalWordRankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    RankConfiguration configuration;

    @BeforeEach
    void setUp() throws IOException {
        query = "dodge charger";

        docs = Files
                .lines(samplesPath)
                .skip(1)
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

        configuration = new RankConfiguration(
                "price",
                null,
                false,
                List.of("viewCount", "creationDate"),
                Set.of("dodge")
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rankByOptionalWords() {
        List<Doc> result = OptionalWordRanker.rankByOptionalWords(docs, query);

        assertEquals(1, result.get(0).getId());
    }

    @Test
    void isWordInDoc() {
        String word = "dodge";
        Doc doc = docs.get(0);

        boolean hasWord = OptionalWordRanker.isWordInDoc(word, doc);

        assertTrue(hasWord);
    }

    @Test
    void isWordInDoc_not() {
        String word = "vision";
        Doc doc = docs.get(0);

        boolean hasWord = OptionalWordRanker.isWordInDoc(word, doc);

        assertFalse(hasWord);
    }

    @Test
    void getAllDocWords() {
        Doc doc = docs.get(13);

        List<String> words = OptionalWordRanker.getAllDocWords(doc);

        assertEquals(6, words.size());
    }

    @Test
    void groupDocsByPhaseScore() {
        docs.get(0).setPhaseScore(7);

        SortedMap<Integer, List<Doc>> result = OptionalWordRanker.groupDocsByPhaseScore(docs);

        assertEquals(7, result.firstKey());
    }

}