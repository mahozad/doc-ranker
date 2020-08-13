package ir.parsijoo.searchia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doc implements Comparable<Doc> {

    static class MinDistance {
        public final int value;
        public final Query.QueryType query;

        public MinDistance(int value, Query.QueryType query) {
            this.value = value;
            this.query = query;
        }
    }

    static class MinPosition {
        public final int value;
        public final String attributeName;

        public MinPosition(int value, String attributeName) {
            this.value = value;
            this.attributeName = attributeName;
        }
    }

    private int id;
    private int numberOfTypos;
    private double elasticScore;
    private Map<String, ?> filterableAttrs;
    private Map<String, ?> customRankingAttrs;
    private Map<String, TokenInfo> tokens = new HashMap<>();
    private List<Attribute<String>> searchableAttrs;
    private MinDistance minDistance;
    private MinPosition minPosition;
    private int numberOfMatches;
    private int numberOfExactMatches;
    private int rank = 0;

    public Doc(int id, Map<String, ?> customRankingAttrs, double elasticScore, List<Attribute<String>> searchableAttrs) {
        this.id = id;
        this.customRankingAttrs = customRankingAttrs;
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

    public int getNumberOfTypos() {
        return numberOfTypos;
    }

    public void setNumberOfTypos(int numberOfTypos) {
        this.numberOfTypos = numberOfTypos;
    }

    public Map<String, ?> getFilterableAttrs() {
        return filterableAttrs;
    }

    public void setFilterableAttrs(Map<String, ?> filterableAttrs) {
        this.filterableAttrs = filterableAttrs;
    }

    public Map<String, TokenInfo> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, TokenInfo> tokens) {
        this.tokens = tokens;
    }

    public int getNumberOfMatches() {
        return numberOfMatches;
    }

    public void setNumberOfMatches(int numberOfMatches) {
        this.numberOfMatches = numberOfMatches;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public MinDistance getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(MinDistance minDistance) {
        this.minDistance = minDistance;
    }

    public MinPosition getMinPosition() {
        return minPosition;
    }

    public void setMinPosition(MinPosition minPosition) {
        this.minPosition = minPosition;
    }

    public int getNumberOfExactMatches() {
        return numberOfExactMatches;
    }

    public void setNumberOfExactMatches(int numberOfExactMatches) {
        this.numberOfExactMatches = numberOfExactMatches;
    }

    public Map<String, ?> getCustomRankingAttrs() {
        return customRankingAttrs;
    }

    public void setCustomRankingAttrs(Map<String, ?> customRankingAttrs) {
        this.customRankingAttrs = customRankingAttrs;
    }

    @Override
    public int compareTo(Doc other) {
        return rank - other.rank;
    }
}