package ir.parsijoo.searchia.rank;

import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.parse.RecordParser;

import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.model.Query.QueryType.*;
import static java.util.Comparator.comparingInt;

public class TypoRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        List<Query> neededQueries = List.of(queries.get(ORIGINAL), queries.get(WILDCARD));
        boolean queriesContainCorrectedOrSuggested = queriesContainCorrectedOrSuggested(queries);

        for (Record record : records) {
            int typos = computeNumberOfTypos(neededQueries, record);
            record.setNumberOfTypos(typos);
        }

        if (queriesContainCorrectedOrSuggested) {
            new RankUpdater<>(records, Record::getNumberOfTypos, phase.getSortDirection()).updateRanks();
        } else {
            records.sort(comparingInt(Record::getNumberOfTypos));
            records.forEach(record -> record.setNumberOfTypos(0));
        }
    }

    private static int computeNumberOfTypos(List<Query> queries, Record record) {
        int numberOfTypos = 0;
        for (Query query : queries) {
            boolean isRecordMatched = RecordParser.isRecordMatchedWithQuery(record, query);
            if (isRecordMatched) {
                return 0;
            } else {
                numberOfTypos = 1;
            }
        }
        return numberOfTypos;
    }

    public static boolean queriesContainCorrectedOrSuggested(Map<QueryType, Query> queries) {
        return queries.containsKey(CORRECTED) || queries.containsKey(SUGGESTED);
    }
}
