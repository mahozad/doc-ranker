package ir.parsijoo.searchia;


import ir.parsijoo.searchia.Query.QueryType;

import java.util.*;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.Query.QueryType.OPTIONAL;
import static ir.parsijoo.searchia.Query.QueryType.ORIGINAL;
import static java.util.stream.Collectors.toSet;

public class OptionalWordRanker {

    /**
     * If we do not have optional query then number of matches in all the docs is same and is equal
     * to number of words of the original query even if there is a longer query and a doc
     * has matched all its words.
     *
     * @param queries
     * @param docs
     * @return
     */
    public static List<Doc> rankByOptionalWords(Map<QueryType, Query> queries, List<Doc> docs) {
        int lengthOfOriginalQuery = queries.get(ORIGINAL).getTokens().size();
        if (!queries.containsKey(OPTIONAL)) {
            docs.forEach(doc -> doc.setNumberOfMatches(lengthOfOriginalQuery));
        } else {
            int lengthOfOptionalQuery = queries.get(OPTIONAL).getTokens().size();
            Set<Query> rankQueries = queries.values().stream().filter(q -> q.getType() != OPTIONAL).collect(toSet());
            for (Doc doc : docs) {
                for (Query query : rankQueries) {
                    if (TypoRanker.isDocMatchedWithQuery(doc, query)) {
                        doc.setNumberOfMatches(lengthOfOriginalQuery);
                        break;
                    }
                }
                doc.setNumberOfMatches(Math.max(doc.getNumberOfMatches(), lengthOfOptionalQuery));
            }
            Ranker.updateRanks(docs, Doc::getNumberOfMatches, true);
        }
        return docs;
    }

    public static SortedMap<Long, List<Doc>> groupDocsByRank(List<Doc> docs) {
        Map<Long, List<Doc>> map = docs.stream().collect(Collectors.groupingBy(Doc::getRank));
        return new TreeMap<>(map);
    }
}
