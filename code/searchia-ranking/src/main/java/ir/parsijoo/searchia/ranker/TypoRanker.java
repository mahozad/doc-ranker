package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.Record;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.processor.RecordProcessor;

import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.Comparator.comparingInt;

public class TypoRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        List<Query> neededQueries = List.of(queries.get(ORIGINAL), queries.get(WILDCARD));
        for (Record record : records) {
            computeNumberOfTypos(neededQueries, record);
        }

        boolean queriesContainCorrectedOrSuggested = queriesContainCorrectedOrSuggested(queries);
        if (queriesContainCorrectedOrSuggested) {
            RankingExecutor.updateRanks(records, Record::getNumberOfTypos, phase.getSortDirection());
        }
        records.sort(comparingInt(Record::getNumberOfTypos));
    }

    private static void computeNumberOfTypos(List<Query> queries, Record record) {
        for (Query query : queries) {
            boolean isRecordMatched = RecordProcessor.isRecordMatchedWithQuery(record, query);
            if (isRecordMatched) {
                record.setNumberOfTypos(0);
                break;
            } else {
                record.setNumberOfTypos(1);
            }
        }
    }

    public static boolean queriesContainCorrectedOrSuggested(Map<QueryType, Query> queries) {
        return queries.containsKey(CORRECTED) || queries.containsKey(SUGGESTED);
    }
}
