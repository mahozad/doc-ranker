package ir.parsijoo.searchia.rank;

import ir.parsijoo.searchia.config.RankingPhase;
import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;

import java.util.List;
import java.util.Map;

public interface Ranker {

    void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phaseInfo);
}
