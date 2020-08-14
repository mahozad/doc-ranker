package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.Record;
import ir.parsijoo.searchia.config.RankingPhase;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CustomRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        String attributeName = phase.getAttributeName();
        Object attr = records.get(0).getCustomRankingAttrs().get(attributeName);
        if (attr instanceof Boolean) {
            Function<Record, Boolean> function = record -> (Boolean) record.getCustomRankingAttrs().get(attributeName);
            RankingExecutor.updateRanks(records, function, phase.getSortDirection());
        } else if (attr instanceof Double) {
            Function<Record, Double> function = record -> (Double) record.getCustomRankingAttrs().get(attributeName);
            RankingExecutor.updateRanks(records, function, phase.getSortDirection());
        } else {
            throw new RuntimeException("The attribute \"" + attributeName + "\" provided for custom ranking is not of type Boolean or Double");
        }
    }
}
