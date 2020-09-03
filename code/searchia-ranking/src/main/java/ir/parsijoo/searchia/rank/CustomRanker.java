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
        Selector<Comparable> selector = record -> record.getCustomRankingAttrs().get(attributeName);
        new RankUpdater<>(records, selector, phase.getSortDirection()).updateRanks();
    }
}
