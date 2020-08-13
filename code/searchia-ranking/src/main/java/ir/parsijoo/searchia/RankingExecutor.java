package ir.parsijoo.searchia;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class RankingExecutor {

    public static List<Doc> executeRanking(
            Map<Query.QueryType, Query> queries,
            List<Doc> docs,
            List<Promotion> promotions,
            RankConfiguration configuration,
            EnumMap<RankingPhase, Integer> phaseOrders,
            int offset,
            int limit) throws IOException {

        DocumentProcessor.processDocs(docs, QueryProcessor.processQueries(queries));

        List<RankingPhase> phases = phaseOrders.entrySet().stream().sorted(comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).collect(toList());
        for (RankingPhase phase : phases) {
            switch (phase) {
                case TYPO:
                    new TypoRanker().rank(queries, docs);
                    break;
                case OPTIONAL_WORDS:
                    new OptionalWordRanker().rank(queries, docs);
                    break;
                case WORDS_DISTANCE:
                    new DistanceRanker().rank(queries, docs);
                    break;
                case WORDS_POSITION:
                    new PositionRanker().rank(queries, docs);
                    break;
                case EXACT_MATCH:
                    new ExactMatchRanker().rank(queries, docs);
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
        Comparator<Doc> comparator = Comparator.comparing(function);
        if (reversed) {
            comparator = comparator.reversed();
        }
        for (List<Doc> group : groups.values()) {
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

    public static SortedMap<Integer, List<Doc>> groupDocsByRank(List<Doc> docs) {
        Map<Integer, List<Doc>> map = docs.stream().collect(Collectors.groupingBy(Doc::getRank));
        return new TreeMap<>(map);
    }
}
