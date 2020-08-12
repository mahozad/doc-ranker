package ir.parsijoo.searchia;

import java.util.List;
import java.util.Set;

public class RankConfiguration {

    private String sortAttribute;
    private double[] geoLocation;
    private boolean shouldRemoveDuplicates;
    private final List<String> customRankingAttrs;
    private final Set<String> queryOptionalWords;
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

    public List<String> getCustomRankingAttrs() {
        return customRankingAttrs;
    }
}
