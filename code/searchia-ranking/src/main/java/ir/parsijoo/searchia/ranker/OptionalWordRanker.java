package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.parser.RecordParser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.model.Query.QueryType.OPTIONAL;
import static ir.parsijoo.searchia.model.Query.QueryType.ORIGINAL;
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
                    if (RecordParser.isRecordMatchedWithQuery(record, query)) {
                        record.setNumberOfMatches(lengthOfOriginalQuery);
                        break;
                    }
                }
            }
            new RankUpdater<>(records, Record::getNumberOfMatches, phase.getSortDirection()).updateRanks();
        } else {
            records.forEach(record -> record.setNumberOfMatches(lengthOfOriginalQuery));
        }
    }
}
