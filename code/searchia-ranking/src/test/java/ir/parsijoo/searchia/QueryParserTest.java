package ir.parsijoo.searchia;

import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.parse.QueryParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static ir.parsijoo.searchia.model.Query.QueryType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class QueryParserTest {

    @Test
    void parseQueries() throws IOException {
        Query query1 = new Query("Dodge charter", ORIGINAL);
        Query query2 = new Query("dodge charter*", WILDCARD);
        Query query3 = new Query("Red dodge Charger", SUGGESTED);
        Map<QueryType, Query> queries = Map.of(
                ORIGINAL, query1,
                WILDCARD, query2,
                SUGGESTED, query3
        );
        List<String> expectedQ1Tokens = List.of("dodge", "charter");
        List<String> expectedQ2Tokens = List.of("dodge", "charter");
        List<String> expectedQ3Tokens = List.of("red", "dodge", "charger");

        QueryParser.parseQueries(queries);

        assertThat(queries.get(ORIGINAL).getTokens(), is(equalTo(expectedQ1Tokens)));
        assertThat(queries.get(WILDCARD).getTokens(), is(equalTo(expectedQ2Tokens)));
        assertThat(queries.get(SUGGESTED).getTokens(), is(equalTo(expectedQ3Tokens)));
    }

}
