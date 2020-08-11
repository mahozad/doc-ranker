package ir.parsijoo.searchia;

import java.util.List;

public class Query {

    enum QueryType {
        ORIGINAL, WILDCARD, CORRECTED, SUGGESTED, SPACED, EQUIVALENT, STEM, OPTIONAL
    }

    private final String text;
    private final QueryType type;
    private List<String> tokens;

    public Query(String text, QueryType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public QueryType getType() {
        return type;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }
}
