package ir.parsijoo.searchia;

import java.util.*;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.Query.QueryType.*;
import static java.util.stream.Collectors.toSet;

public class ExactMatchRanker {

    private static final Set<Query.QueryType> queryTypes = Set.of(ORIGINAL, WILDCARD, SPACED, EQUIVALENT);

    public static List<Doc> rankByExactMatch(Map<Query.QueryType, Query> queries, List<Doc> docs) {
        int lengthOfOriginalQuery = queries.get(ORIGINAL).getTokens().size();
        for (Doc doc : docs) {
            for (Query.QueryType queryType : queries.keySet().stream().filter(queryTypes::contains).collect(toSet())) {
                Query query = queries.get(queryType);
                int numberOfMatches = DocumentProcessor.getNumberOfMatches(doc, query);
                numberOfMatches = Math.min(numberOfMatches, lengthOfOriginalQuery);
                if (queryType == WILDCARD) {
                    doc.setNumberOfExactMatches(Math.max(doc.getNumberOfExactMatches(), numberOfMatches - 1));
                } else if (numberOfMatches == lengthOfOriginalQuery) {
                    doc.setNumberOfExactMatches(numberOfMatches);
                    break;
                } else {
                    doc.setNumberOfExactMatches(Math.max(doc.getNumberOfExactMatches(), numberOfMatches));
                }
            }
        }

        int rank = 0; // Rank starts from 0 (top doc has rank of 0)
        SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
        for (long rankOfGroupMembers : groups.keySet()) {
            List<Doc> group = groups.get(rankOfGroupMembers);
            // TODO: This code is duplicate in other ranker classes
            List<Doc> sortedGroup = group.stream().sorted(Comparator.comparingInt(Doc::getNumberOfExactMatches).reversed()).collect(Collectors.toList());
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

        return docs;
    }
}
