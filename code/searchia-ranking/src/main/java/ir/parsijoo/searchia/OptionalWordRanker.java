package ir.parsijoo.searchia;


import ir.parsijoo.searchia.Query.QueryType;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        Query originalQuery = queries.get(QueryType.ORIGINAL);
        int lengthOfOriginalQuery = DocumentProcessor.tokenizeText(originalQuery.getText()).size();
        if (!queries.containsKey(QueryType.OPTIONAL)) {
            for (Doc doc : docs) {
                doc.setNumberOfMatches(lengthOfOriginalQuery);
            }
        } else {
            Query optionalQuery = queries.get(QueryType.OPTIONAL);
            int lengthOfOptionalQuery = DocumentProcessor.tokenizeText(optionalQuery.getText()).size();
            SortedMap<Long, List<Doc>> docGroups = groupDocsByRank(docs);

            int rank = 0; // Rank starts from 0 (top doc has rank of 0)
            for (List<Doc> group : docGroups.values()) {
                for (Doc doc : group) {
                    for (Query query : queries.values().stream().filter(query -> query.getType() != QueryType.OPTIONAL).collect(Collectors.toList())) {
                        if (TypoRanker.isDocMatchedWithQuery(doc, query)) {
                            doc.setNumberOfMatches(lengthOfOriginalQuery);
                            break;
                        }
                    }
                    doc.setNumberOfMatches(Math.max(doc.getNumberOfMatches(), lengthOfOptionalQuery));
                }

                List<Doc> sortedGroup = group.stream().sorted((doc1, doc2) -> doc2.getNumberOfMatches() - doc1.getNumberOfMatches()).collect(Collectors.toList());
                long previousNumberOfMatches = sortedGroup.get(0).getNumberOfMatches();
                for (Doc doc : sortedGroup) {
                    if (doc.getNumberOfMatches() != previousNumberOfMatches) {
                        rank++;
                        doc.setRank(rank);
                        previousNumberOfMatches = doc.getNumberOfMatches();
                    } else {
                        doc.setRank(rank);
                    }
                }
                rank++;
            }
        }
        return docs;
    }

    public static SortedMap<Long, List<Doc>> groupDocsByRank(List<Doc> docs) {
        Map<Long, List<Doc>> map = docs.stream().collect(Collectors.groupingBy(Doc::getRank));
        return new TreeMap<>(map);
    }
}
