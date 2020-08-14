package ir.parsijoo.searchia;

import ir.parsijoo.searchia.dto.RankingDTO;
import ir.parsijoo.searchia.dto.RankingPhaseDTO;
import ir.parsijoo.searchia.dto.RankingPhaseType;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.dto.RankingPhaseType.*;
import static java.util.stream.Collectors.toList;

public class RankingExecutor {

    private static final Map<RankingPhaseType, Ranker> rankers = Map.of(
            TYPO, new TypoRanker(),
            OPTIONAL_WORDS, new OptionalWordRanker(),
            WORDS_DISTANCE, new DistanceRanker(),
            WORDS_POSITION, new PositionRanker(),
            EXACT_MATCH, new ExactMatchRanker(),
            CUSTOM, new CustomRanker()
    );

    public static List<Doc> executeRanking(
            Map<Query.QueryType, Query> queries,
            List<Doc> docs,
            List<Promotion> promotions,
            RankingDTO rankingDTO,
            int offset,
            int limit) throws IOException {

        QueryProcessor.processQueries(queries);
        DocumentProcessor.processDocs(docs);

        List<RankingPhaseDTO> phases = rankingDTO
                .getPhases()
                .stream()
                .filter(RankingPhaseDTO::isEnabled)
                .sorted()
                .collect(toList());

        for (RankingPhaseDTO phaseInfo : phases) {
            Ranker ranker = rankers.get(phaseInfo.getType());
            ranker.rank(queries, docs, phaseInfo);
        }

        docs.sort(Doc::compareTo);
        return docs.subList(offset, limit);
    }

    public static <T extends Comparable<T>> void updateRanks(List<Doc> docs, Function<Doc, T> function, boolean reversed) {
        Comparator<Doc> comparator = Comparator.comparing(function);
        if (reversed) {
            comparator = comparator.reversed();
        }

        int rank = 0; // Rank starts from 0 (top doc has rank of 0)
        SortedMap<Integer, List<Doc>> groups = groupDocsByRank(docs);
        for (List<Doc> group : groups.values()) {
            List<Doc> sortedGroup = group.stream().sorted(comparator).collect(toList());
            T currentValue = function.apply(sortedGroup.get(0));
            for (Doc doc : sortedGroup) {
                T attributeValue = function.apply(doc);
                if (attributeValue.compareTo(currentValue) != 0) {
                    rank++;
                    currentValue = attributeValue;
                }
                doc.setRank(rank);
            }
            rank++;
        }
    }

    public static SortedMap<Integer, List<Doc>> groupDocsByRank(List<Doc> docs) {
        Map<Integer, List<Doc>> map = docs.stream().collect(Collectors.groupingBy(Doc::getRank));
        return new TreeMap<>(map);
    }
}
