package ir.parsijoo.searchia.rank;

import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.parse.RecordParser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.model.Query.QueryType.*;
import static java.util.stream.Collectors.toSet;

public class ExactMatchRanker implements Ranker {

    private static final Set<QueryType> queryTypes = Set.of(ORIGINAL, WILDCARD, SPACED, EQUIVALENT);

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        int lengthOfOriginalQuery = queries.get(ORIGINAL).getTokens().size();
        Set<Query> rankQueries = queries.values().stream().filter(q -> queryTypes.contains(q.getType())).collect(toSet());
        for (Record record : records) {
            for (Query query : rankQueries) {
                int numberOfMatches = RecordParser.getNumberOfMatches(record, query);
                numberOfMatches = Math.min(numberOfMatches, lengthOfOriginalQuery);
                if (query.getType() == WILDCARD) {
                    record.setNumberOfExactMatches(Math.max(record.getNumberOfExactMatches(), numberOfMatches - 1));
                } else if (numberOfMatches == lengthOfOriginalQuery) {
                    record.setNumberOfExactMatches(numberOfMatches);
                    break;
                } else {
                    record.setNumberOfExactMatches(Math.max(record.getNumberOfExactMatches(), numberOfMatches));
                }
            }
        }
        new RankUpdater<>(records, Record::getNumberOfExactMatches, phase.getSortDirection()).updateRanks();
    }
}
