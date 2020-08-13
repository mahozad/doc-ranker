package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class QueryProcessor {

    public static Map<String, Set<QueryType>> processQueries(Map<QueryType, Query> queries) throws IOException {
        Map<String, Set<Query.QueryType>> result = new HashMap<>();
        for (Entry<QueryType, Query> entry : queries.entrySet()) {
            QueryType queryType = entry.getKey();
            Query query = entry.getValue();

            List<String> tokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(query.getText());
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                if (queryType == QueryType.WILDCARD && i == tokens.size() - 1) {
                    token += "*"; // because the tokenizer removes the *
                }
                Set<QueryType> queryTypes = new HashSet<>();
                queryTypes.add(queryType);
                result.merge(token, queryTypes, (set1, set2) -> {
                    set1.addAll(set2);
                    return set1;
                });
            }

            query.setTokens(tokens);
        }
        return result;
    }
}
