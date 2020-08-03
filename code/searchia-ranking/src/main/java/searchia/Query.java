package searchia;

public class Query {

    enum QueryType {
        ORIGINAL, WILDCARD, CORRECTED, SUGGESTED, SPACED, EQUIVALENT, STEM, OPTIONAL
    }

    private String text;
    private QueryType type;

    public Query(String text, QueryType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QueryType getType() {
        return type;
    }

    public void setType(QueryType type) {
        this.type = type;
    }
}
