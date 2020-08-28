package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.config.RankingConfig;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.config.RankingPhaseType;
import ir.parsijoo.searchia.config.SortDirection;
import ir.parsijoo.searchia.parser.QueryParser;
import ir.parsijoo.searchia.parser.RecordParser;
import ir.parsijoo.searchia.ranker.*;

import java.io.IOException;
import java.util.*;

import static ir.parsijoo.searchia.config.RankingPhaseType.*;
import static ir.parsijoo.searchia.config.SortDirection.DESCENDING;
import static java.util.stream.Collectors.groupingBy;
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

    public static List<Record> executeRanking(
            Map<QueryType, Query> queries,
            List<Record> records,
            List<Promotion> promotions,
            RankingConfig rankingConfig,
            int offset, int limit) throws IOException {

        QueryParser.parseQueries(queries);
        RecordParser.parseRecords(records);

        rankingConfig
                .getPhases()
                .stream()
                .filter(RankingPhase::isEnabled)
                .sorted()
                .forEach(phase -> rankers.get(phase.getType()).rank(queries, records, phase));

        records.sort(Record::compareTo);
        return records.subList(offset, limit);
    }

    public static <T extends Comparable<T>> void updateRanks(List<Record> records,
                                                             Selector<T> selector,
                                                             SortDirection sortDirection) {
        Comparator<Record> comparator = Comparator.comparing(selector::get);
        if (sortDirection == DESCENDING) {
            comparator = comparator.reversed();
        }

        int rank = 0; // Rank starts from 0 (i.e. top record has rank of 0)
        SortedMap<Integer, List<Record>> groups = groupRecordsByRank(records);
        for (List<Record> group : groups.values()) {
            List<Record> sortedGroup = group.stream().sorted(comparator).collect(toList());
            T currentValue = selector.get(sortedGroup.get(0));
            for (Record record : sortedGroup) {
                T attributeValue = selector.get(record);
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
