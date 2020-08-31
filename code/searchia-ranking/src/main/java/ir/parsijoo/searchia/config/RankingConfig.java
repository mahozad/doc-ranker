package ir.parsijoo.searchia.config;

import ir.parsijoo.searchia.model.Record;

import java.util.List;
import java.util.Set;

public class RankingConfig {

    private final Set<RankingPhase> phases;
    private final List<Record> promotions;
    private final int offset;
    private final int limit;

    public RankingConfig(Set<RankingPhase> phases, List<Record> promotions, int offset, int limit) {
        this.phases = phases;
        this.promotions = promotions;
        this.offset = offset;
        this.limit = limit;
    }

    public Set<RankingPhase> getPhases() {
        return phases;
    }

    public List<Record> getPromotions() {
        return promotions;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }
}
