package ir.parsijoo.searchia.dto;

import java.io.Serializable;

public class QueryDTO implements Serializable {

    public enum QueryTypeDTO {
        ORIGINAL, WILDCARD, CORRECTED, SUGGESTED, SPACED, EQUIVALENT, STEM, OPTIONAL
    }

    private final QueryTypeDTO type;
    private final String text;

    public QueryDTO(QueryTypeDTO type, String text) {
        this.text = text;
        this.type = type;
    }

    public QueryTypeDTO getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
