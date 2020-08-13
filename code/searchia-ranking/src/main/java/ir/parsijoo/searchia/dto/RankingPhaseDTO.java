package ir.parsijoo.searchia.dto;

import java.io.Serializable;

public class RankingPhaseDTO implements Serializable {

    private final RankingPhaseType type;
    private final boolean enabled;
    private final int order;
    private final SortDirection sortDirection;

    public RankingPhaseDTO(RankingPhaseType type, boolean enabled, int order, SortDirection sortDirection) {
        this.order = order;
        this.type = type;
        this.enabled = enabled;
        this.sortDirection = sortDirection;
    }

    public RankingPhaseType getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getOrder() {
        return order;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }
}
