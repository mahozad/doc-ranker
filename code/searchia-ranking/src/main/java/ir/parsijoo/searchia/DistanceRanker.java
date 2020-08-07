package ir.parsijoo.searchia;


import java.util.*;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.DocumentProcessor.ATTRIBUTES_DISTANCE;


public class DistanceRanker {

    private static final int WORDS_DISTANCE_IN_DIFFERENT_ATTRIBUTES = 8;
    private static final int MAX_WORDS_DISTANCE_IN_SAME_ATTRIBUTE = 7;

    public static List<Doc> rankByWordsDistance(Map<Query.QueryType, Query> queries, List<Doc> docs) {
        for (Doc doc : docs) {
            Doc.MinDistance minDistance = getDocMinDistanceFromQueries(doc, queries);
            doc.setMinDistance(minDistance);
        }
        SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
        int rank = 0; // Rank starts from 0 (top doc has rank of 0)
        for (long rankOfGroupMembers : groups.keySet()) {
            List<Doc> group = groups.get(rankOfGroupMembers);
            // TODO: This code is duplicate of OptionalWordRanker
            List<Doc> sortedGroup = group.stream().sorted(Comparator.comparingInt(d -> d.getMinDistance().value)).collect(Collectors.toList());
            long previousDistance = sortedGroup.get(0).getMinDistance().value;
            for (Doc doc : sortedGroup) {
                if (doc.getMinDistance().value != previousDistance) {
                    rank++;
                    doc.setRank(rank);
                    previousDistance = doc.getMinDistance().value;
                } else {
                    doc.setRank(rank);
                }
            }
            rank++;
        }
        return docs;
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
     * This page seems to provide an algorithm to calculate min list distance:
     * https://leetcode.com/articles/shortest-word-distance-ii/
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
            int position1 = positions1.get(i);
            int position2 = positions2.get(j);

            if (position1 < position2) {
                penalty = 0; // first word has occurred before the second word in the text
            } else {
                penalty = 1; // first word has occurred after the second word in the text
            }

            int distance = Math.abs(position1 - position2) + penalty;
            minDistance = Math.min(minDistance, distance);

            if (position1 < position2 && i < positions1.size() - 1) {
                i++;
            } else {
                j++;
            }
        }

        if (minDistance > ATTRIBUTES_DISTANCE / 2) {
            // The two words (and so their distance) were from different attributes
            return WORDS_DISTANCE_IN_DIFFERENT_ATTRIBUTES;
        } else {
            return Math.min(minDistance, MAX_WORDS_DISTANCE_IN_SAME_ATTRIBUTE);
        }
    }

    public static Doc.MinDistance getDocMinDistanceFromQueries(Doc doc, Map<Query.QueryType, Query> queries) {
        int minDistance = Integer.MAX_VALUE;
        Query.QueryType selectedQueryType = Query.QueryType.ORIGINAL;
        for (Query.QueryType queryType : queries.keySet()) {
            Query query = queries.get(queryType);
            int distance = calculateDocDistanceFromQuery(doc, query);
            if (distance < minDistance) {
                minDistance = distance;
                selectedQueryType = queryType;
            }
        }
        return new Doc.MinDistance(minDistance, selectedQueryType);
    }
}
