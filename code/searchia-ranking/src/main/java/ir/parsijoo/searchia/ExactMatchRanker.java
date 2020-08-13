package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.stream.Collectors.toSet;

public class ExactMatchRanker implements Ranker {

    private static final Set<QueryType> queryTypes = Set.of(ORIGINAL, WILDCARD, SPACED, EQUIVALENT);

    @Override
    public void rank(Map<QueryType, Query> queries, List<Doc> docs) {
        int lengthOfOriginalQuery = queries.get(ORIGINAL).getTokens().size();
        Set<Map.Entry<QueryType, Query>> rankQueries = queries.entrySet().stream().filter(q -> queryTypes.contains(q.getKey())).collect(toSet());
        for (Doc doc : docs) {
            for (Map.Entry<QueryType, Query> entry : rankQueries) {
                QueryType queryType = entry.getKey();
                Query query = entry.getValue();
                int numberOfMatches = doc.getQueryToNumberOfMatches().get(queryType);
                numberOfMatches = Math.min(numberOfMatches, lengthOfOriginalQuery);
                if (query.getType() == WILDCARD) {
                    doc.setNumberOfExactMatches(Math.max(doc.getNumberOfExactMatches(), numberOfMatches - 1));
                } else if (numberOfMatches == lengthOfOriginalQuery) {
                    doc.setNumberOfExactMatches(numberOfMatches);
                    break;
                } else {
                    doc.setNumberOfExactMatches(Math.max(doc.getNumberOfExactMatches(), numberOfMatches));
                }
            }
        }
        RankingExecutor.updateRanks(docs, Doc::getNumberOfExactMatches, true);
    }
}
