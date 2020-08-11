package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.stream.Collectors.toSet;

public class ExactMatchRanker {

    private static final Set<QueryType> queryTypes = Set.of(ORIGINAL, WILDCARD, SPACED, EQUIVALENT);

    public static void rankByExactMatch(Map<QueryType, Query> queries, List<Doc> docs) {
        int lengthOfOriginalQuery = queries.get(ORIGINAL).getTokens().size();
        Set<Query> rankQueries = queries.values().stream().filter(q -> queryTypes.contains(q.getType())).collect(toSet());
        for (Doc doc : docs) {
            for (Query query : rankQueries) {
                int numberOfMatches = DocumentProcessor.getNumberOfMatches(doc, query);
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
        Ranker.updateRanks(docs, Doc::getNumberOfExactMatches, true);
    }
}
