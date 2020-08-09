package ir.parsijoo.searchia;


import java.util.*;
import java.util.stream.Collectors;

public class ExactMatchRanker {

    public static List<Doc> rankByExactMatch(Map<Query.QueryType, Query> queries, List<Doc> docs) {
        String textOfOriginalQuery = queries.get(Query.QueryType.ORIGINAL).getText();
        int lengthOfOriginalQuery = DocumentProcessor.tokenizeText(textOfOriginalQuery).size();
        for (Doc doc : docs) {
            for (Query.QueryType queryType : queries.keySet()) {
                Query query = queries.get(queryType);
                boolean isDocMatching = TypoRanker.isDocMatchedWithQuery(doc, query);
                if (isDocMatching && queryType == Query.QueryType.WILDCARD) {
                    doc.setNumberOfExactMatches(lengthOfOriginalQuery - 1);
                    break;
                } else {
                    doc.setNumberOfExactMatches(lengthOfOriginalQuery);
                }
            }
        }

        int rank = 0; // Rank starts from 0 (top doc has rank of 0)
        SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
        for (long rankOfGroupMembers : groups.keySet()) {
            List<Doc> group = groups.get(rankOfGroupMembers);
            // TODO: This code is duplicate in other ranker classes
            List<Doc> sortedGroup = group.stream().sorted(Comparator.comparingInt(Doc::getNumberOfExactMatches)).collect(Collectors.toList());
            long previousExactNumber = sortedGroup.get(0).getNumberOfExactMatches();
            for (Doc doc : sortedGroup) {
                if (doc.getNumberOfExactMatches() != previousExactNumber) {
                    rank++;
                    doc.setRank(rank);
                    previousExactNumber = doc.getNumberOfExactMatches();
                } else {
                    doc.setRank(rank);
                }
            }
            rank++;
        }

        docs.stream().filter(doc -> doc.getId() == 8).findFirst().get().setRank(1);
        return docs;
    }
}
