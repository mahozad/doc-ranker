package ir.parsijoo.searchia;

import java.io.IOException;
import java.util.*;

public class Ranker {

    public static List<Doc> rank(
            Map<Query.QueryType, Query> queries,
            List<Doc> docs,
            List<Promotion> promotions,
            RankConfiguration configuration,
            int offset,
            int limit) throws IOException {

        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDocs(docs);

        List<Doc> sortedByTypo = TypoRanker.rankByTypo(queries, docs);
        List<Doc> sortedByOptionalWords = OptionalWordRanker.rankByOptionalWords(queries, sortedByTypo);
        List<Doc> sortedByWordsDistance = DistanceRanker.rankByWordsDistance(queries, sortedByOptionalWords);
        List<Doc> sortedByWordPosition = PositionRanker.rankByWordPosition(sortedByWordsDistance, queries);
        List<Doc> sortedByExactMatch = ExactMatchRanker.rankByExactMatch(queries, sortedByWordPosition);
        List<Doc> finalResult = CustomRanker.rankByCustomAttributes(sortedByExactMatch, configuration.getCustomRankingAttrs());

        List<Doc> sublist = finalResult.subList(offset, limit);
        sublist.sort(Doc::compareTo);
        return sublist;
    }
}

class RankConfiguration {

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

class Doc implements Comparable<Doc> {

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
    private long rank = 0;

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

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
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
        return (int) (rank - other.rank);
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
