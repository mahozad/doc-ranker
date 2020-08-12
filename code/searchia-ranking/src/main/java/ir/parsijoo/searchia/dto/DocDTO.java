package ir.parsijoo.searchia.dto;

import java.io.Serializable;
import java.util.Map;

public class DocDTO implements Serializable {

    private final int id; // optional
    private final double backendScore; // optional
    // Example: customRankingAttrs = Map.of("viewCount", 100, "rating", 4.3, "boosted", false);
    private final Map<String, ? extends Comparable<?>> customRankingAttrs; // Map from attribute name to its value. Values should be comparable, for example numeric or boolean
    private final Map<String, String> searchableAttrs; // Map from attribute name to its text. Example: "title" -> "Coronavirus"

    public DocDTO(int id, double backendScore, Map<String, ? extends Comparable<?>> customRankingAttrs, Map<String, String> searchableAttrs) {
        this.id = id;
        this.backendScore = backendScore;
        this.customRankingAttrs = customRankingAttrs;
        this.searchableAttrs = searchableAttrs;
    }

    public int getId() {
        return id;
    }

    public double getBackendScore() {
        return backendScore;
    }

    public Map<String, ? extends Comparable<?>> getCustomRankingAttrs() {
        return customRankingAttrs;
    }

    public Map<String, String> getSearchableAttrs() {
        return searchableAttrs;
    }
}
