package ir.parsijoo.searchia;

import ir.parsijoo.searchia.dto.RankingPhaseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.dto.RankingPhaseType.CUSTOM;
import static ir.parsijoo.searchia.dto.SortDirection.DESCENDING;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CustomRankerTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    RankConfiguration configuration;
    CustomRanker ranker;

    @BeforeEach
    void setUp() throws IOException {
        query = "dodge charger";

        docs = Files
                .lines(samplesPath)
                .filter(line -> !line.startsWith("#"))
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    double creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    double viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    double score = Math.random();
                    Attribute<String> title = new Attribute<>(attrs[3].split("=")[0], attrs[3].split("=")[1]);
                    Attribute<String> description = new Attribute<>(attrs[4].split("=")[0], attrs[4].split("=")[1]);
                    List<Attribute<String>> searchableAttrs = List.of(title, description);
                    Map<String, Double> customAttrs = Map.of("viewCount", viewCount, "creationDate", creationDate);
                    return new Doc(id, customAttrs, score, searchableAttrs);
                })
                .collect(toList());

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

        ranker = new CustomRanker();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rankByCustomAttributes_viewCount() {
        DocumentProcessor.processDocs(docs);
        List<Integer> expectedRanks = List.of(9, 6, 9, 6, 6, 10, 6, 8, 4, 1, 0, 5, 2, 3, 7, 2, 2);
        RankingPhaseDTO phase = new RankingPhaseDTO(CUSTOM, true, 0, DESCENDING, "viewCount");

        ranker.rank(null, docs, phase);

        assertThat(docs.stream().map(Doc::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }

    @Test
    void rankByCustomAttributes_creationDate() {
        DocumentProcessor.processDocs(docs);
        List<Integer> expectedRanks = List.of(11, 0, 13, 4, 14, 10, 6, 7, 3, 12, 9, 2, 1, 8, 5, 9, 11);
        RankingPhaseDTO phase = new RankingPhaseDTO(CUSTOM, true, 0, DESCENDING, "creationDate");

        ranker.rank(null, docs, phase);

        assertThat(docs.stream().map(Doc::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }

    @Test
    void rankByCustomAttributes_bothAttributes() {
        DocumentProcessor.processDocs(docs);
        List<Integer> expectedRanks = List.of(14, 8, 15, 9, 11, 16, 10, 13, 6, 1, 0, 7, 2, 5, 12, 3, 4);
        RankingPhaseDTO phase1 = new RankingPhaseDTO(CUSTOM, true, 0, DESCENDING, "viewCount");
        RankingPhaseDTO phase2 = new RankingPhaseDTO(CUSTOM, true, 0, DESCENDING, "creationDate");

        ranker.rank(null, docs, phase1);
        ranker.rank(null, docs, phase2);

        assertThat(docs.stream().map(Doc::getRank).collect(toList()), is(equalTo(expectedRanks)));
    }
}
