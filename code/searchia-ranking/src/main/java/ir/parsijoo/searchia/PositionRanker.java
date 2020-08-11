package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;

import java.util.*;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.DocumentProcessor.ATTRIBUTES_DISTANCE;

public class PositionRanker {

    public static List<Doc> rankByWordPosition(List<Doc> docs, Map<QueryType, Query> queries) {
        for (Doc doc : docs) {
            int minPosition = Integer.MAX_VALUE;
            for (Query query : queries.values()) {
                int minPositionFromQuery = getDocMinWordPositionByQuery(doc, query);
                if (minPositionFromQuery < minPosition) {
                    minPosition = minPositionFromQuery;
                }
            }
            // FIXME: The attribute name is set to a constant value
            doc.setMinPosition(new Doc.MinPosition(minPosition, "title"));
        }

        SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
        int rank = 0; // Rank starts from 0 (top doc has rank of 0)
        for (List<Doc> group : groups.values()) {
            // TODO: This code is duplicate in other ranker classes
            List<Doc> sortedGroup = group.stream().sorted(Comparator.comparingInt(d -> d.getMinPosition().value)).collect(Collectors.toList());
            long previousMinPosition = sortedGroup.get(0).getMinPosition().value;
            for (Doc doc : sortedGroup) {
                if (doc.getMinPosition().value != previousMinPosition) {
                    rank++;
                    doc.setRank(rank);
                    previousMinPosition = doc.getMinPosition().value;
                } else {
                    doc.setRank(rank);
                }
            }
            rank++;
        }

        return docs;
    }

    public static int getDocMinWordPositionByQuery(Doc doc, Query query) {
        int minPosition = Integer.MAX_VALUE;
        for (String qWord : query.getTokens()) {
            if (doc.getTokens().containsKey(qWord)) {
                List<Integer> tokenPositions = doc.getTokens().get(qWord).getPositions();
                Optional<Integer> min = tokenPositions.stream().min(Comparator.comparingInt(p -> p % ATTRIBUTES_DISTANCE));
                if (min.isPresent() && min.get() < minPosition) {
                    minPosition = min.get() % ATTRIBUTES_DISTANCE;
                }
            }
            // A slight optimization: because the best position is 0 we do not continue the loop
            if (minPosition == 0) {
                break;
            }
        }
        return minPosition;
    }
}
