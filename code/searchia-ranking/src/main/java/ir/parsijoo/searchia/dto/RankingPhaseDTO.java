package ir.parsijoo.searchia.dto;

import java.io.Serializable;

public class RankingPhaseDTO implements Serializable {

    private RankingPhaseType type;
    private boolean enabled;
    private int order;
    private SortDirection sortDirection;

    public RankingPhaseDTO(RankingPhaseType type, boolean enabled, int order, SortDirection sortDirection) {
        this.order = order;
        this.type = type;
        this.enabled = enabled;
        this.sortDirection = sortDirection;
    }
}
