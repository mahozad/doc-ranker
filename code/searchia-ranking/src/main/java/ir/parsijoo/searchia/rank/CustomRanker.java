package ir.parsijoo.searchia.rank;

import ir.parsijoo.searchia.Selector;
import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;

import java.util.List;
import java.util.Map;

public class CustomRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        String attributeName = phase.getAttributeName();
        Object attr = records.get(0).getCustomRankingAttrs().get(attributeName);
        if (attr instanceof Number) {
            Selector<Double> selector = record -> ((Number) record.getCustomRankingAttrs().get(attributeName)).doubleValue();
            new RankUpdater<>(records, selector, phase.getSortDirection()).updateRanks();
        } else if (attr instanceof Boolean) {
            Selector<Boolean> selector = record -> (Boolean) record.getCustomRankingAttrs().get(attributeName);
            new RankUpdater<>(records, selector, phase.getSortDirection()).updateRanks();
        } else {
            throw new UnsupportedAttributeTypeException(attributeName);
        }
    }

    public static class UnsupportedAttributeTypeException extends RuntimeException {
        public UnsupportedAttributeTypeException(String attributeName) {
            super(String.format("The attribute \"%s\" is not numeric or boolean", attributeName));
        }
    }
}
