package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.Comparator.comparingInt;

public class TypoRanker implements Ranker {

    private static int lengthOfOriginalQuery;
    private static int lengthOfWildcardQuery;

    @Override
    public void rank(Map<QueryType, Query> queries, List<Doc> docs) {
        lengthOfOriginalQuery = queries.get(ORIGINAL).getTokens().size();
        lengthOfWildcardQuery = queries.get(WILDCARD).getTokens().size();
        for (Doc doc : docs) {
            computeNumberOfTypos(doc);
        }
        boolean queriesContainCorrectedOrSuggested = queriesContainCorrectedOrSuggested(queries);
        if (queriesContainCorrectedOrSuggested) {
            RankingExecutor.updateRanks(docs, Doc::getNumberOfTypos, false);
        } else {
            docs.sort(comparingInt(Doc::getNumberOfTypos));
        }
    }

    private static void computeNumberOfTypos(Doc doc) {
        if (doc.getQueryToNumberOfMatches().get(ORIGINAL) == lengthOfOriginalQuery ||
                doc.getQueryToNumberOfMatches().get(WILDCARD) == lengthOfWildcardQuery) {
            doc.setNumberOfTypos(0);
        } else {
            doc.setNumberOfTypos(1);
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
