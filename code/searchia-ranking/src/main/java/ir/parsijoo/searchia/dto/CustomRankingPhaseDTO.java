package ir.parsijoo.searchia.dto;

import java.io.Serializable;

public class CustomRankingPhaseDTO extends RankingPhaseDTO implements Serializable {

    /**
     * Name of the attribute used for this custom phase. Example: "viewCount"
     */
    private final String attributeName;

    public CustomRankingPhaseDTO(PhaseType type, boolean enabled, int order, SortDirection sortDirection, String attributeName) {
        super(type, enabled, order, sortDirection);
        this.attributeName = attributeName;
        validateType(type);
    }

    private void validateType(PhaseType type) {
        if (type != PhaseType.CUSTOM) {
            throw new IllegalArgumentException("The type for a CustomPhaseDTO should be PhaseType.CUSTOM but was " + type);
        }
    }

    public String getAttributeName() {
        return attributeName;
    }
}
