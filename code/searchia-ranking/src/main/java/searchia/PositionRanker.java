package searchia;

import searchia.Doc.MinPosition;
import searchia.Query.QueryType;

import java.util.*;
import java.util.stream.Collectors;

import static searchia.DocumentProcessor.ATTRIBUTES_DISTANCE;

public class PositionRanker {

    public static List<Doc> rankByWordPosition(List<Doc> docs, Map<QueryType, Query> queries) {
        for (Doc doc : docs) {
            int minPosition = Integer.MAX_VALUE;
            for (QueryType queryType : queries.keySet()) {
                Query query = queries.get(queryType);
                int minPositionFromQuery = getDocMinWordPositionByQuery(doc, query);
                if (minPositionFromQuery < minPosition) {
                    minPosition = minPositionFromQuery;
                }
            }
            // FIXME: The attribute name is set to a constant value
            doc.setMinPosition(new MinPosition(minPosition, "title"));
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
        List<String> qWords = DocumentProcessor.tokenizeText(query.getText());
        for (String qWord : qWords) {
            if (doc.getTokens().containsKey(qWord)) {
                List<Integer> tokenPositions = doc.getTokens().get(qWord).getPositions();
                Optional<Integer> min = tokenPositions.stream().min(Comparator.comparingInt(p -> p % ATTRIBUTES_DISTANCE));
                if (min.isPresent() && min.get() < minPosition) {
                    minPosition = min.get() % ATTRIBUTES_DISTANCE;
                }
            }
        }
        return minPosition;
    }
}
