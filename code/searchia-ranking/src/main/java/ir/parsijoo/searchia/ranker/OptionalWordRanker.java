package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.Record;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.processor.RecordProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.Query.QueryType.OPTIONAL;
import static ir.parsijoo.searchia.Query.QueryType.ORIGINAL;
import static java.util.stream.Collectors.toSet;

public class OptionalWordRanker implements Ranker {

    /**
     * If we do not have optional query then number of matches in all the records is same and is equal
     * to number of words of the original query even if there is a longer query and a record
     * has matched all its words.
     *
     * @param queries
     * @param records
     * @param phase
     * @return
     */
    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        int lengthOfOriginalQuery = queries.get(ORIGINAL).getTokens().size();
        if (queries.containsKey(OPTIONAL)) {
            int lengthOfOptionalQuery = queries.get(OPTIONAL).getTokens().size();
            Set<Query> neededQueries = queries.values().stream().filter(q -> q.getType() != OPTIONAL).collect(toSet());
            for (Record record : records) {
                record.setNumberOfMatches(lengthOfOptionalQuery);
                for (Query query : neededQueries) {
                    if (RecordProcessor.isRecordMatchedWithQuery(record, query)) {
                        record.setNumberOfMatches(lengthOfOriginalQuery);
                        break;
                    }
                }
            }
            RankingExecutor.updateRanks(records, Record::getNumberOfMatches, phase.getSortDirection());
        } else {
            records.forEach(record -> record.setNumberOfMatches(lengthOfOriginalQuery));
        }
    }
}
