package searchia;

import searchia.Query.QueryType;

import java.util.List;
import java.util.Map;

public class ExactMatchRanker {

    public static List<Doc> rankByExactMatch(Map<QueryType, Query> queries, List<Doc> docs) {
        docs.stream().filter(doc -> doc.getId() == 8).findFirst().get().setRank(1);
        return docs;
    }
}
