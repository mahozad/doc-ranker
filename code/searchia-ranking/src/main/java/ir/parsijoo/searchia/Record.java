package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Record implements Comparable<Record> {

    public static class MinDistance {
        public final int value;
        public final QueryType query;

        public MinDistance(int value, QueryType query) {
            this.value = value;
            this.query = query;
        }
    }

    public static class MinPosition {
        public final int value;
        public final String attributeName;

        public MinPosition(int value, String attributeName) {
            this.value = value;
            this.attributeName = attributeName;
        }
    }

    private int id;
    private int numberOfTypos = 0; // NOTE: The default value should be 0
    private double elasticScore;
    private Map<String, ?> filterableAttrs;
    private Map<String, ? extends Comparable<?>> customRankingAttrs;
    private Map<String, List<Integer>> tokens = new HashMap<>();
    private Map<String, String> searchableAttrs;
    private MinDistance minDistance;
    private MinPosition minPosition;
    private int numberOfMatches;
    private int numberOfExactMatches;
    private int rank = 0;

    public Record(int id, Map<String, ? extends Comparable<?>> customRankingAttrs, double elasticScore, Map<String, String> searchableAttrs) {
        this.id = id;
        this.customRankingAttrs = customRankingAttrs;
        this.elasticScore = elasticScore;
        this.searchableAttrs = searchableAttrs;
        this.filterableAttrs = Collections.emptyMap();
    }

    public int getId() {
        return id;
    }

    public Map<String, String> getSearchableAttrs() {
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

    public Map<String, List<Integer>> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, List<Integer>> tokens) {
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

    public Map<String, ? extends Comparable<?>> getCustomRankingAttrs() {
        return customRankingAttrs;
    }

    public void setCustomRankingAttrs(Map<String, ? extends Comparable<?>> customRankingAttrs) {
        this.customRankingAttrs = customRankingAttrs;
    }

    @Override
    public int compareTo(Record otherRecord) {
        return this.rank - otherRecord.rank;
    }
}
