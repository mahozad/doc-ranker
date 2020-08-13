package ir.parsijoo.searchia.dto;

import java.io.Serializable;

public class RankingPhaseDTO implements Serializable {

    enum PhaseType {
        TYPO, OPTIONAL_WORDS, WORDS_DISTANCE, WORDS_POSITION, EXACT_MATCH, CUSTOM
    }

    private PhaseType type;
    private boolean enabled;
    private int order;
    private SortDirection sortDirection;

    public RankingPhaseDTO(PhaseType type, boolean enabled, int order, SortDirection sortDirection) {
        this.order = order;
        this.type = type;
        this.enabled = enabled;
        this.sortDirection = sortDirection;
    }
}
