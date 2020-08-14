package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Doc;
import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.config.RankingPhase;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ir.parsijoo.searchia.processor.DocumentProcessor.ATTRIBUTES_DISTANCE;

public class PositionRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Doc> docs, RankingPhase phase) {
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
        RankingExecutor.updateRanks(docs, doc -> doc.getMinPosition().value, phase.getSortDirection());
    }

    public static int getDocMinWordPositionByQuery(Doc doc, Query query) {
        int minPosition = Integer.MAX_VALUE;
        for (String qWord : query.getTokens()) {
            if (doc.getTokens().containsKey(qWord)) {
                List<Integer> tokenPositions = doc.getTokens().get(qWord);
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