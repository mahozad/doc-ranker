import java.util.List;

public class Ranker {

    public static List<Doc> rank(
            String query,
            List<Doc> matches,
            List<Promotion> promotions,
            RankConfiguration configuration,
            int offset,
            int limit) {

        return List.of(
                new Doc(1),
                new Doc(2),
                new Doc(3),
                new Doc(4),
                new Doc(5),
                new Doc(6),
                new Doc(7),
                new Doc(8),
                new Doc(9),
                new Doc(10)
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
    private List<Attribute> attrs;

    public Doc(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

class Attribute {

    private String name;
    private Object value;
}

class Promotion {

    private Doc doc;
    private int positionInResult;
}
