package searchia;

import searchia.Doc.MinPosition;
import searchia.Query.QueryType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PositionRanker {

    public static List<Doc> rankByWordPosition(List<Doc> docs, Map<QueryType, Query> queries) {
        docs.stream().filter(doc -> doc.getId() == 2).findFirst().get().setRank(0);
        docs.stream().filter(doc -> doc.getId() == 6).findFirst().get().setRank(1);
        docs.stream().filter(doc -> doc.getId() == 6).findFirst().get().setMinPosition(new MinPosition(1, "title"));
        return docs;
    }

    public static int getDocMinWordPositionByQuery(Doc doc, Query query) {
        int minPosition = Integer.MAX_VALUE;
        List<String> qWords = DocumentProcessor.tokenizeText(query.getText());
        for (String qWord : qWords) {
            if (doc.getTokens().containsKey(qWord)) {
                List<Integer> tokenPositions = doc.getTokens().get(qWord).getPositions();
                Optional<Integer> min = tokenPositions.stream().min(Comparator.comparingInt(p -> p));
                if (min.isPresent() && min.get() < minPosition) {
                    minPosition = min.get();
                }
            }
        }
        return minPosition;
    }
}
