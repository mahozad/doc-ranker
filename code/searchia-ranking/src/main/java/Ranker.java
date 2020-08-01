import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Ranker {

    public static List<Doc> rank(
            String query,
            List<Doc> docs,
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
    private Set<String> queryOptionalWords;
    private Set<Filter<?>> selectedFilters;

    public RankConfiguration(String sortAttribute,
                             double[] geoLocation,
                             boolean shouldRemoveDuplicates,
                             List<String> customRankingAttrs,
                             Set<String> queryOptionalWords) {
        this.sortAttribute = sortAttribute;
        this.geoLocation = geoLocation;
        this.shouldRemoveDuplicates = shouldRemoveDuplicates;
        this.customRankingAttrs = customRankingAttrs;
        this.queryOptionalWords = queryOptionalWords;
    }

    public Set<String> getQueryOptionalWords() {
        return queryOptionalWords;
    }

    public Set<Filter<?>> getSelectedFilters() {
        return selectedFilters;
    }

    public void setSelectedFilters(Set<Filter<?>> selectedFilters) {
        this.selectedFilters = selectedFilters;
    }
}

class Doc implements Comparable<Doc> {

    private int id;
    private int phaseScore;
    private double elasticScore;
    private Map<String, ?> customAttrs;
    private Map<String, ?> filterableAttrs;
    private List<Attribute<String>> searchableAttrs;

    public Doc(int id, Map<String, ?> customAttrs, double elasticScore, List<Attribute<String>> searchableAttrs) {
        this.id = id;
        this.customAttrs = customAttrs;
        this.elasticScore = elasticScore;
        this.searchableAttrs = searchableAttrs;
        this.filterableAttrs = Map.of();
    }

    public int getId() {
        return id;
    }

    public List<Attribute<String>> getSearchableAttrs() {
        return searchableAttrs;
    }

    public int getPhaseScore() {
        return phaseScore;
    }

    public void setPhaseScore(int phaseScore) {
        this.phaseScore = phaseScore;
    }

    public Map<String, ?> getFilterableAttrs() {
        return filterableAttrs;
    }

    public void setFilterableAttrs(Map<String, ?> filterableAttrs) {
        this.filterableAttrs = filterableAttrs;
    }

    @Override
    public int compareTo(Doc other) {
        return phaseScore - other.phaseScore;
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
