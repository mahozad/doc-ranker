package ir.parsijoo.searchia;



import ir.parsijoo.searchia.Query.QueryType;

import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

public class TypoRanker {

    public static List<Doc> rankByTypo(Map<QueryType, Query> queries, List<Doc> docs) {
        boolean queriesContainCorrectedOrSuggested = queriesContainCorrectedOrSuggested(queries);
        long topGroupCount = 0;

        List<Query> rankQueries = List.of(queries.get(ORIGINAL), queries.get(WILDCARD));
        for (Doc doc : docs) {
            for (Query query : rankQueries) {
                boolean isDocMatching = isDocMatchedWithQuery(doc, query);
                if (isDocMatching) {
                    doc.setNumberOfTypos(1);
                    topGroupCount++;
                    break;
                } else {
                    int score = Math.max(0, doc.getNumberOfTypos());
                    doc.setNumberOfTypos(score);
                }
            }
        }

        if (queriesContainCorrectedOrSuggested) {
            // Example: if the top group has 7 members, all its members should be ranked 0 and
            // all members of second group should be ranked 7
            for (Doc doc : docs) {
                if (doc.getNumberOfTypos() == 0) {
                    doc.setRank(topGroupCount);
                }
            }
        }

        return docs;
    }

    public static boolean isDocMatchedWithQuery(Doc doc, Query query) {
        List<String> tokens = DocumentProcessor.tokenizeText(query.getText());
        for (String token : tokens) {
            if (query.getType() == WILDCARD && token.endsWith("*")) {
                String tokenStem = token.replace("*", "");
                boolean noMatches = doc.getTokens().keySet().stream().noneMatch(s -> s.startsWith(tokenStem));
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
