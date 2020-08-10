package ir.parsijoo.searchia;


public class Query {

    enum QueryType {
        ORIGINAL, WILDCARD, CORRECTED, SUGGESTED, SPACED, EQUIVALENT, STEM, OPTIONAL
    }

    private final String text;
    private final QueryType type;

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
}
