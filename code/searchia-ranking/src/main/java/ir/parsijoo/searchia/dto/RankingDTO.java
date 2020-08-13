package ir.parsijoo.searchia.dto;

import java.io.Serializable;
import java.util.Set;

public class RankingDTO implements Serializable {

    private final Set<RankingPhaseDTO> phases;

    public RankingDTO(Set<RankingPhaseDTO> phases) {
        this.phases = phases;
    }

    public Set<RankingPhaseDTO> getPhases() {
        return phases;
    }
}
