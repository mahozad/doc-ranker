package ir.parsijoo.searchia.processor;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class QueryProcessor {

    public static void processQueries(Map<QueryType, Query> queries) throws IOException {
        for (Query query : queries.values()) {
            List<String> tokens = RecordProcessor.tokenizeTextWithoutAddingPositions(query.getText());
            query.setTokens(tokens);
        }
    }
}
