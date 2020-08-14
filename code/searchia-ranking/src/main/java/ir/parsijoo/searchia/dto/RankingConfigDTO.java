package ir.parsijoo.searchia.dto;

import java.io.Serializable;
import java.util.Set;

public class RankingConfigDTO implements Serializable {

    private final Set<RankingPhaseDTO> phases;

    public RankingConfigDTO(Set<RankingPhaseDTO> phases) {
        this.phases = phases;
    }

    public Set<RankingPhaseDTO> getPhases() {
        return phases;
    }
}
