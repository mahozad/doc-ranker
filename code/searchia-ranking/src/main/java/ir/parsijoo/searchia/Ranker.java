package ir.parsijoo.searchia;

import java.util.List;
import java.util.Map;

public interface Ranker {

    void rank(Map<Query.QueryType, Query> queries, List<Doc> docs);
}
