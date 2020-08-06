package searchia;

import searchia.Doc.MinPosition;
import searchia.Query.QueryType;

import java.util.List;
import java.util.Map;

public class PositionRanker {

    public static List<Doc> rankByWordPosition(List<Doc> docs, Map<QueryType, Query> queries) {
        docs.stream().filter(doc -> doc.getId() == 2).findFirst().get().setRank(0);
        docs.stream().filter(doc -> doc.getId() == 6).findFirst().get().setRank(1);
        docs.stream().filter(doc -> doc.getId() == 6).findFirst().get().setMinPosition(new MinPosition(1, "title"));
        return docs;
    }
}
