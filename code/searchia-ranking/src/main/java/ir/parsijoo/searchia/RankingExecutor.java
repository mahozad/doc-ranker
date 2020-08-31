package ir.parsijoo.searchia;

import ir.parsijoo.searchia.config.RankingConfig;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.config.RankingPhaseType;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.parse.QueryParser;
import ir.parsijoo.searchia.parse.RecordParser;
import ir.parsijoo.searchia.rank.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.config.RankingPhaseType.*;
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
            RankingConfig config) throws IOException {
        QueryParser.parseQueries(queries);
        RecordParser.parseRecords(records);
        runPhases(queries, records, config);
        records.sort(Record::compareTo);
        return records.subList(config.getOffset(), config.getLimit());
    }

    private static void runPhases(Map<QueryType, Query> queries, List<Record> records, RankingConfig config) {
        List<RankingPhase> phases = getPhases(config);
        for (RankingPhase phase : phases) {
            rankers.get(phase.getType()).rank(queries, records, phase);
        }
    }

    private static List<RankingPhase> getPhases(RankingConfig config) {
        return config
                .getPhases()
                .stream()
                .filter(RankingPhase::isEnabled)
                .sorted()
                .collect(toList());
    }
}
