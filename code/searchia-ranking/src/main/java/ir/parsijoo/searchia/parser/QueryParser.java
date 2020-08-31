package ir.parsijoo.searchia.parser;

import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class QueryParser {

    public static void parseQueries(Map<QueryType, Query> queries) throws IOException {
        for (Query query : queries.values()) {
            List<String> tokens = RecordParser.tokenizeTextWithoutAddingPositions(query.getText());
            query.setTokens(tokens);
        }
    }
}
