import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Ranker {

    public static List<Doc> rank(
            String query,
            List<Doc> matches,
            List<Promotion> promotions,
            RankConfiguration configuration,
            int offset,
            int limit) {

        return List.of(
                new Doc(1, null, 0, Collections.emptyList()),
                new Doc(2, null, 0, Collections.emptyList()),
                new Doc(3, null, 0, Collections.emptyList()),
                new Doc(4, null, 0, Collections.emptyList()),
                new Doc(5, null, 0, Collections.emptyList()),
                new Doc(6, null, 0, Collections.emptyList()),
                new Doc(7, null, 0, Collections.emptyList()),
                new Doc(8, null, 0, Collections.emptyList()),
                new Doc(9, null, 0, Collections.emptyList()),
                new Doc(10, null, 0, Collections.emptyList())
        );
    }
}

class RankConfiguration {

    private String sortAttribute;
    private double[] geoLocation;
    private boolean shouldRemoveDuplicates;
    private List<String> customRankingAttrs;

    public RankConfiguration(String sortAttribute,
                             double[] geoLocation,
                             boolean shouldRemoveDuplicates,
                             List<String> customRankingAttrs) {
        this.sortAttribute = sortAttribute;
        this.geoLocation = geoLocation;
        this.shouldRemoveDuplicates = shouldRemoveDuplicates;
        this.customRankingAttrs = customRankingAttrs;
    }
}

class Doc {

    private int id;
    private double score;
    private Map<String, ?> customAttrs;
    private List<Attribute<String>> searchableAttrs;

    public Doc(int id, Map<String, ?> customAttrs, double score, List<Attribute<String>> searchableAttrs) {
        this.id = id;
        this.customAttrs = customAttrs;
        this.score = score;
        this.searchableAttrs = searchableAttrs;
    }

    public int getId() {
        return id;
    }
}

class Attribute<T> {

    private String name;
    private T value;

    public Attribute(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

class Promotion {

    private Doc doc;
    private int positionInResult;
}
