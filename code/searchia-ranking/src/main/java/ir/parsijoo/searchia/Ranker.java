package ir.parsijoo.searchia;

import ir.parsijoo.searchia.dto.RankingPhaseDTO;

import java.util.List;
import java.util.Map;

public interface Ranker {

    void rank(Map<Query.QueryType, Query> queries, List<Doc> docs, RankingPhaseDTO phaseInfo);
}
