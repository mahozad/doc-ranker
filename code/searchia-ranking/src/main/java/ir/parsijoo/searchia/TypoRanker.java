package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.Comparator.comparingInt;

public class TypoRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Doc> docs, RankConfiguration config) {
        boolean queriesContainCorrectedOrSuggested = queriesContainCorrectedOrSuggested(queries);
        List<Query> rankQueries = List.of(queries.get(ORIGINAL), queries.get(WILDCARD));
        for (Doc doc : docs) {
            computeNumberOfTypos(rankQueries, doc);
        }
        if (queriesContainCorrectedOrSuggested) {
            RankingExecutor.updateRanks(docs, Doc::getNumberOfTypos, false);
        }
        docs.sort(comparingInt(Doc::getNumberOfTypos));
    }

    private static void computeNumberOfTypos(List<Query> queries, Doc doc) {
        for (Query query : queries) {
            boolean isDocMatching = isDocMatchedWithQuery(doc, query);
            if (isDocMatching) {
                doc.setNumberOfTypos(0);
                break;
            } else {
                doc.setNumberOfTypos(1);
            }
        }
    }

    public static boolean isDocMatchedWithQuery(Doc doc, Query query) {
        Iterator<String> tokens = query.getTokens().iterator();
        while (tokens.hasNext()) {
            String token = tokens.next();
            // If queryType is WILDCARD, the last word should be considered as prefix
            if (query.getType() == WILDCARD && !tokens.hasNext()) {
                boolean noMatches = doc.getTokens().keySet().stream().noneMatch(s -> s.startsWith(token));
                if (noMatches) {
                    return false;
                }
            } else if (!doc.getTokens().containsKey(token)) {
                return false;
            }
        }
        return true;
    }

    public static boolean queriesContainCorrectedOrSuggested(Map<QueryType, Query> queries) {
        return queries.containsKey(CORRECTED) || queries.containsKey(SUGGESTED);
    }
}
