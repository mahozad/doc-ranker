package ir.parsijoo.searchia;

import java.util.Set;

public class RankingConfig {

    private final Set<RankingPhase> phases;

    public RankingConfig(Set<RankingPhase> phases) {
        this.phases = phases;
    }

    public Set<RankingPhase> getPhases() {
        return phases;
    }
}
