package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.Record;
import ir.parsijoo.searchia.config.RankingPhase;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.Comparator.comparingInt;

public class TypoRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        boolean queriesContainCorrectedOrSuggested = queriesContainCorrectedOrSuggested(queries);
        List<Query> rankQueries = List.of(queries.get(ORIGINAL), queries.get(WILDCARD));
        for (Record record : records) {
            computeNumberOfTypos(rankQueries, record);
        }
        if (queriesContainCorrectedOrSuggested) {
            RankingExecutor.updateRanks(records, Record::getNumberOfTypos, phase.getSortDirection());
        }
        records.sort(comparingInt(Record::getNumberOfTypos));
    }

    private static void computeNumberOfTypos(List<Query> queries, Record record) {
        for (Query query : queries) {
            boolean isRecordMatching = isRecordMatchedWithQuery(record, query);
            if (isRecordMatching) {
                record.setNumberOfTypos(0);
                break;
            } else {
                record.setNumberOfTypos(1);
            }
        }
    }

    public static boolean isRecordMatchedWithQuery(Record record, Query query) {
        Iterator<String> tokens = query.getTokens().iterator();
        while (tokens.hasNext()) {
            String token = tokens.next();
            // If queryType is WILDCARD, the last word should be considered as prefix
            if (query.getType() == WILDCARD && !tokens.hasNext()) {
                boolean noMatches = record.getTokens().keySet().stream().noneMatch(s -> s.startsWith(token));
                if (noMatches) {
                    return false;
                }
            } else if (!record.getTokens().containsKey(token)) {
                return false;
            }
        }
        return true;
    }

    public static boolean queriesContainCorrectedOrSuggested(Map<QueryType, Query> queries) {
        return queries.containsKey(CORRECTED) || queries.containsKey(SUGGESTED);
    }
}
