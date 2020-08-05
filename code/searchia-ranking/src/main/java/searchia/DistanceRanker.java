package searchia;

import searchia.Doc.MinDistance;
import searchia.Query.QueryType;

import java.util.*;
import java.util.stream.Collectors;

import static searchia.DocumentProcessor.ATTRIBUTES_DISTANCE;

public class DistanceRanker {

    public static List<Doc> rankByWordsDistance(Map<QueryType, Query> queries, List<Doc> docs) {
        for (Doc doc : docs) {
            MinDistance minDistance = getDocMinDistanceFromQueries(doc, queries);
            doc.setMinDistance(minDistance);
        }
        List<Doc> result = new ArrayList<>();
        SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
        for (List<Doc> group : groups.values()) {
            // TODO: This code is duplicate of OptionalWordRanker
            List<Doc> sortedGroup = group.stream().sorted(Comparator.comparingInt(d -> d.getMinDistance().value)).collect(Collectors.toList());
            long previousDistance = sortedGroup.get(0).getMinDistance().value;
            long rank = sortedGroup.get(0).getRank();
            for (Doc doc : sortedGroup) {
                if (doc.getMinDistance().value != previousDistance) {
                    rank++;
                    doc.setRank(rank);
                    previousDistance = doc.getMinDistance().value;
                } else {
                    doc.setRank(rank);
                }
            }
            result.addAll(sortedGroup);
        }
        return result;
    }

    public static int calculateDocDistanceFromQuery(Doc doc, Query query) {
        List<String> qWords = DocumentProcessor.tokenizeText(query.getText());
        int i = 0;
        int totalDistance = 0;
        String word1;
        String word2;
        while (i + 1 < qWords.size()) {
            word1 = qWords.get(i);
            word2 = qWords.get(i + 1);
            if (!doc.getTokens().containsKey(word1) || !doc.getTokens().containsKey(word2)) {
                return Integer.MAX_VALUE;
            }
            List<Integer> positions1 = doc.getTokens().get(word1).getPositions();
            List<Integer> positions2 = doc.getTokens().get(word2).getPositions();
            int minDistance = calculateMinDistanceBetweenTwoPositionLists(positions1, positions2);
            totalDistance += minDistance;
            i++;
        }
        return totalDistance;
    }

    /**
     * positions1 and positions2 must be sorted in ascending order.
     *
     * @param positions1
     * @param positions2
     * @return
     */
    public static int calculateMinDistanceBetweenTwoPositionLists(List<Integer> positions1,
                                                                  List<Integer> positions2) {
        int i = 0;
        int j = 0;
        int minDistance = Integer.MAX_VALUE;
        int penalty = 0;

        while (i < positions1.size() && j < positions2.size()) {
            if (positions1.get(i) < positions2.get(j)) {
                // first word has occurred before the second word in the text
                penalty = 0;
            } else {
                // first word has occurred after the second word in the text
                penalty = 1;
            }
            int distance = Math.abs(positions1.get(i) - positions2.get(j)) + penalty;
            if (distance < minDistance) {
                minDistance = distance;
            }

            if (positions1.get(i) < positions2.get(j)) {
                if (i < positions1.size() - 1) {
                    i++;
                } else {
                    j++;
                }
            } else {
                if (j < positions2.size() - 1) {
                    j++;
                } else {
                    i++;
                }
            }
        }

        if (minDistance > ATTRIBUTES_DISTANCE / 2) {
            // The words were in different attributes so their distance is 8
            return 8;
        } else {
            return Math.min(7, minDistance);
        }
    }

    public static MinDistance getDocMinDistanceFromQueries(Doc doc, Map<QueryType, Query> queries) {
        int minDistance = Integer.MAX_VALUE;
        QueryType selectedQueryType = QueryType.ORIGINAL;
        for (QueryType queryType : queries.keySet()) {
            Query query = queries.get(queryType);
            int distance = calculateDocDistanceFromQuery(doc, query);
            if (distance < minDistance) {
                minDistance = distance;
                selectedQueryType = queryType;
            }
        }
        return new MinDistance(minDistance, selectedQueryType);
    }
}
