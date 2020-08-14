package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.Record;
import ir.parsijoo.searchia.Record.MinDistance;
import ir.parsijoo.searchia.config.RankingPhase;

import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.processor.RecordProcessor.ATTRIBUTES_DISTANCE;

public class DistanceRanker implements Ranker {

    private static final int WORDS_DISTANCE_IN_DIFFERENT_ATTRIBUTES = 8;
    private static final int MAX_WORDS_DISTANCE_IN_SAME_ATTRIBUTE = 7;

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        for (Record record : records) {
            MinDistance minDistance = getRecordMinDistanceFromQueries(record, queries);
            record.setMinDistance(minDistance);
        }
        RankingExecutor.updateRanks(records, record -> record.getMinDistance().value, phase.getSortDirection());
    }

    public static MinDistance getRecordMinDistanceFromQueries(Record record, Map<QueryType, Query> queries) {
        int minDistance = Integer.MAX_VALUE;
        QueryType theQueryContainingMinDistance = QueryType.ORIGINAL;
        for (Query query : queries.values()) {
            int distance = calculateRecordDistanceFromQuery(record, query);
            if (distance < minDistance) {
                minDistance = distance;
                theQueryContainingMinDistance = query.getType();
            }
        }
        return new MinDistance(minDistance, theQueryContainingMinDistance);
    }

    public static int calculateRecordDistanceFromQuery(Record record, Query query) {
        List<String> qWords = query.getTokens();
        int i = 0;
        int totalDistance = 0;
        String word1;
        String word2;
        while (i < qWords.size() - 1) {
            word1 = qWords.get(i);
            word2 = qWords.get(i + 1);
            if (!record.getTokens().containsKey(word1) || !record.getTokens().containsKey(word2)) {
                return Integer.MAX_VALUE;
            }
            List<Integer> positions1 = record.getTokens().get(word1);
            List<Integer> positions2 = record.getTokens().get(word2);
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
}
