package ir.parsijoo.searchia;

import ir.parsijoo.searchia.dto.RankDTO.RankingPhase;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static ir.parsijoo.searchia.OptionalWordRanker.groupDocsByRank;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class Ranker {

    public static List<Doc> rank(
            Map<Query.QueryType, Query> queries,
            List<Doc> docs,
            List<Promotion> promotions,
            RankConfiguration configuration,
            List<RankingPhase> rankingPhases,
            int offset,
            int limit) throws IOException {

        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDocs(docs);

        List<RankingPhase> phases = rankingPhases.stream().sorted(comparingInt(RankingPhase::getOrder)).collect(toList());
        for (RankingPhase rankingPhase : phases) {
            switch (rankingPhase) {
                case TYPO:
                    TypoRanker.rankByTypo(queries, docs);
                    break;
                case NUMBER_OF_WORDS:
                    OptionalWordRanker.rankByOptionalWords(queries, docs);
                    break;
                case WORDS_DISTANCE:
                    DistanceRanker.rankByWordsDistance(queries, docs);
                    break;
                case WORDS_POSITION:
                    PositionRanker.rankByWordPosition(queries, docs);
                    break;
                case EXACT_MATCH:
                    ExactMatchRanker.rankByExactMatch(queries, docs);
                    break;
                case CUSTOM:
                    CustomRanker.rankByCustomAttributes(docs, configuration.getCustomRankingAttrs());
                    break;
            }

//            if (docs.size() > limit) {
//                docs.sort(comparingInt(Doc::getRank));
//                int previousRank = docs.get(offset).getRank();
//                for (int i = 0; i < log2(docs.size()) + 1; i++) {
//                    int endIndex = Math.min((int) (offset + limit + Math.pow(2, i) - 1), docs.size() - 1);
//                    int rank = docs.get(endIndex).getRank();
//                    if (rank > previousRank) {
//                        docs = docs.subList(offset, endIndex);
//                        break;
//                    }
//                }
//            }
        }

        docs.sort(Doc::compareTo);
        return docs.subList(offset, limit);
    }

    public static <T extends Comparable<T>> void updateRanks(List<Doc> docs, Function<Doc, T> function, boolean reversed) {
        SortedMap<Integer, List<Doc>> groups = groupDocsByRank(docs);
        int rank = 0; // Rank starts from 0 (top doc has rank of 0)
        for (List<Doc> group : groups.values()) {
            Comparator<Doc> comparator = Comparator.comparing(function);
            if (reversed) {
                comparator = comparator.reversed();
            }
            List<Doc> sortedGroup = group.stream().sorted(comparator).collect(toList());
            T currentValue = function.apply(sortedGroup.get(0));
            for (Doc doc : sortedGroup) {
                if (function.apply(doc).compareTo(currentValue) != 0) {
                    rank++;
                    doc.setRank(rank);
                    currentValue = function.apply(doc);
                } else {
                    doc.setRank(rank);
                }
            }
            rank++;
        }
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
