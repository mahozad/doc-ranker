package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Doc;
import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.config.RankingPhase;

import java.util.List;
import java.util.Map;

public interface Ranker {

    void rank(Map<Query.QueryType, Query> queries, List<Doc> docs, RankingPhase phaseInfo);
}