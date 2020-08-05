package searchia;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import searchia.Query.QueryType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DistanceRankerTest {

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
    void rankByWordsDistance() {
        List<Doc> result = DistanceRanker.rankByWordsDistance(query, docs);

        assertEquals(1, result.get(0).getId());
    }

    @Test
    void calculateDocDistanceByQuery() {
        Doc doc = docs.stream().filter(d -> d.getId() == 2).findFirst().get();
        Query query = new Query("red dodge charger", QueryType.SUGGESTED);

        int distance = DistanceRanker.calculateDocDistanceByQuery(doc, query);

        assertEquals(4, distance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists() {
        List<Integer> positions1 = List.of(1, 2);
        List<Integer> positions2 = List.of(3, 5, 6);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(1, minDistance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists_minDistanceIsInSecondAttribute() {
        List<Integer> positions1 = List.of(2, 1_000_000);
        List<Integer> positions2 = List.of(5, 1_000_001, 2_000_000);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(1, minDistance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists_secondWordComesBeforeFirstWord() {
        List<Integer> positions1 = List.of(14, 15, 30, 31);
        List<Integer> positions2 = List.of(3, 12, 20);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(3, minDistance);
    }

    @Test
    void calculateMinDistanceBetweenTwoPositionLists_wordsAreInDifferentAttributes() {
        List<Integer> positions1 = List.of(4, 15, 30, 31);
        List<Integer> positions2 = List.of(1_000_013, 1_000_014, 1_000_017, 2_000_016);

        int minDistance = DistanceRanker.calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);

        assertEquals(8, minDistance);
    }

    @Test
    void rankByWordsDistance_oneWordQuery() {
    }

    @Disabled
    @Test
    void getWordPositions() {
        // Attribute<String> attribute = new Attribute<>("title", "dodge new and dodge red charger");
        // String word = "dodge";
        //
        // int[] positions = DistanceRanker.getWordPositions(word, attribute);
        //
        // assertThat(positions, is(equalTo(new int[]{0, 3})));
    }
}
