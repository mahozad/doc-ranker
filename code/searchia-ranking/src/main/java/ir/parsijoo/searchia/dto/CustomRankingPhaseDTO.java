package ir.parsijoo.searchia.dto;

import java.io.Serializable;

public class CustomRankingPhaseDTO extends RankingPhaseDTO implements Serializable {

    /**
     * Name of the attribute used for this custom phase. Example: "viewCount"
     */
    private final String attributeName;

    public CustomRankingPhaseDTO(RankingPhaseType type, boolean enabled, int order, SortDirection sortDirection, String attributeName) {
        super(type, enabled, order, sortDirection);
        this.attributeName = attributeName;
        validateType(type);
    }

    private void validateType(RankingPhaseType type) {
        if (type != RankingPhaseType.CUSTOM) {
            throw new IllegalArgumentException("The type for a custom phase should be CUSTOM but was " + type);
        }
    }

    public String getAttributeName() {
        return attributeName;
    }
}
