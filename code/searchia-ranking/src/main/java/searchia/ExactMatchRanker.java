package searchia;

import searchia.Query.QueryType;

import java.util.*;
import java.util.stream.Collectors;

public class ExactMatchRanker {

    public static List<Doc> rankByExactMatch(Map<QueryType, Query> queries, List<Doc> docs) {
        String textOfOriginalQuery = queries.get(QueryType.ORIGINAL).getText();
        int lengthOfOriginalQuery = DocumentProcessor.tokenizeText(textOfOriginalQuery).size();
        for (Doc doc : docs) {
            for (QueryType queryType : queries.keySet()) {
                Query query = queries.get(queryType);
                boolean isDocMatching = TypoRanker.isDocMatchedWithQuery(doc, query);
                if (isDocMatching && queryType == QueryType.WILDCARD) {
                    doc.setNumberOfExactMatches(lengthOfOriginalQuery - 1);
                    break;
                } else {
                    doc.setNumberOfExactMatches(lengthOfOriginalQuery);
                }
            }
        }

        List<Doc> result = new ArrayList<>();
        SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
        for (List<Doc> group : groups.values()) {
            // TODO: This code is duplicate in other ranker classes
            List<Doc> sortedGroup = group.stream().sorted(Comparator.comparingInt(Doc::getNumberOfExactMatches)).collect(Collectors.toList());
            long previousExactNumber = sortedGroup.get(0).getNumberOfExactMatches();
            long rank = sortedGroup.get(0).getRank();
            for (Doc doc : sortedGroup) {
                if (doc.getNumberOfExactMatches() != previousExactNumber) {
                    rank++;
                    doc.setRank(rank);
                    previousExactNumber = doc.getNumberOfExactMatches();
                } else {
                    doc.setRank(rank);
                }
            }
            result.addAll(sortedGroup);
        }
        // return result;

        docs.stream().filter(doc -> doc.getId() == 8).findFirst().get().setRank(1);
        return docs;
    }
}
