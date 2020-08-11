package ir.parsijoo.searchia;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class QueryProcessor {

    public static void processQueries(Map<Query.QueryType, Query> queries) throws IOException {
        for (Query query : queries.values()) {
            List<String> tokens = DocumentProcessor.normalizeText(query.getText());
            query.setTokens(tokens);
        }
    }
}
