import java.util.List;

public class Ranker {

    public static List<Doc> rank(
            String query,
            List<Doc> matches,
            List<Promotion> promotions,
            RankConfiguration configuration,
            int offset,
            int limit) {

        return List.of(new Doc());
    }
}

class RankConfiguration {

    private Attribute sort;
    private double[] geoLocation;
    private boolean removeDuplicates;
    private List<Attribute> customRankingAttrs;
}

class Doc {

    private int id;
    private double score;
    private List<Attribute> attrs;

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
    private int indexInResult;
}
