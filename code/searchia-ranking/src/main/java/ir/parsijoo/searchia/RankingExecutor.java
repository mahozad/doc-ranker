package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.config.RankingConfig;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.config.RankingPhaseType;
import ir.parsijoo.searchia.config.SortDirection;
import ir.parsijoo.searchia.ranker.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static ir.parsijoo.searchia.config.RankingPhaseType.*;
import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static java.util.stream.Collectors.*;

public class RankingExecutor {

    private static final Map<RankingPhaseType, Ranker> rankers = Stream.of(
            new SimpleEntry<>(TYPO, new TypoRanker()),
            new SimpleEntry<>(OPTIONAL_WORDS, new OptionalWordRanker()),
            new SimpleEntry<>(WORDS_DISTANCE, new DistanceRanker()),
            new SimpleEntry<>(WORDS_POSITION, new PositionRanker()),
            new SimpleEntry<>(EXACT_MATCH, new ExactMatchRanker()),
            new SimpleEntry<>(CUSTOM, new CustomRanker())
    ).collect(toMap(Entry::getKey, Entry::getValue));

    public static List<Record> executeRanking(
            Map<QueryType, Query> queries,
            List<Record> records,
            List<Promotion> promotions,
            RankingConfig rankingConfig,
            int offset, int limit) {

        rankingConfig.getPhases()
                .stream()
                .sorted()
                .filter(RankingPhase::isEnabled)
                .filter(phase -> phase.getType() == CUSTOM)
                .forEach(phase -> rankers.get(phase.getType()).rank(queries, records, phase));

        records.sort(Record::compareTo);
        return records.subList(offset, limit);
    }

    public static <T extends Comparable<T>> void updateRanks(List<Record> records,
                                                             Function<Record, T> function,
                                                             SortDirection sortDirection) {
        Comparator<Record> comparator = Comparator.comparing(function);
        if (sortDirection == DESCENDING) {
            comparator = comparator.reversed();
        }

        int rank = 0; // Rank starts from 0 (i.e. top record has rank of 0)
        SortedMap<Integer, List<Record>> groups = groupRecordsByRank(records);
        for (List<Record> group : groups.values()) {
            List<Record> sortedGroup = group.stream().sorted(comparator).collect(toList());
            T currentValue = function.apply(sortedGroup.get(0));
            for (Record record : sortedGroup) {
                T attributeValue = function.apply(record);
                if (attributeValue.compareTo(currentValue) != 0) {
                    rank++;
                    currentValue = attributeValue;
                }
                record.setRank(rank);
            }
            rank++;
        }
    }

    public static SortedMap<Integer, List<Record>> groupRecordsByRank(List<Record> records) {
        Map<Integer, List<Record>> map = records.stream().collect(groupingBy(Record::getRank));
        return new TreeMap<>(map);
    }
}
