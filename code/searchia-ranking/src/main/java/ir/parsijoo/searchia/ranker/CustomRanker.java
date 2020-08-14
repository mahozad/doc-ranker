package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Doc;
import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.config.RankingPhase;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CustomRanker implements Ranker {

    @Override
    public void rank(Map<Query.QueryType, Query> queries, List<Doc> docs, RankingPhase phase) {
        String attributeName = phase.getAttributeName();
        Object attr = docs.get(0).getCustomRankingAttrs().get(attributeName);
        if (attr instanceof Boolean) {
            Function<Doc, Boolean> function = doc -> (Boolean) doc.getCustomRankingAttrs().get(attributeName);
            RankingExecutor.updateRanks(docs, function, phase.getSortDirection());
        } else if (attr instanceof Double) {
            Function<Doc, Double> function = doc -> (Double) doc.getCustomRankingAttrs().get(attributeName);
            RankingExecutor.updateRanks(docs, function, phase.getSortDirection());
        } else {
            throw new RuntimeException("The attribute \"" + attributeName + "\" provided for custom ranking is not of type Boolean or Double");
        }
    }
}
