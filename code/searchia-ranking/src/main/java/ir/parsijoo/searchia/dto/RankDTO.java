package ir.parsijoo.searchia.dto;

import ir.parsijoo.searchia.Query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.dto.RankDTO.RankingPhase.NUMBER_OF_WORDS;
import static ir.parsijoo.searchia.dto.RankDTO.RankingPhase.TYPO;

public class RankDTO {

    enum RankingPhase {
        TYPO(0), NUMBER_OF_WORDS(1), WORDS_DISTANCE(2), WORDS_POSITION(3), EXACT_MATCH(4), CUSTOM(5);
        int order;
        RankingPhase(int order) {
            this.order = order;
        }
    }

    enum SortDirection {
        ASCENDING, DESCENDING
    }

    // T should be either Boolean or Double
    class Attribute<T> {
        String name;
        SortDirection sortType = SortDirection.DESCENDING; // default == DESCENDING
        T value;
    }

    class Promotion {
        Doc doc; // or int docId;
        int positionInResult;
    }

    enum QueryType {
        ORIGINAL, WILDCARD, CORRECTED, SUGGESTED, SPACED, EQUIVALENT, STEM, OPTIONAL
    }

    class Doc {
        int id; // optional
        double elasticScore; // optional
        Map<String, ?> customRankingAttrs; // map from attribute name to its value. example: "title" -> "Coronavirus"
        List<Attribute<String>> searchableAttrs; // attributes that we use their text for ranking
    }

    boolean typoPhaseEnabled = true; // default == true
    Set<RankingPhase> phases = Set.of(TYPO, NUMBER_OF_WORDS /*,...*/); // changing default orders: TYPO.order = 4; EXACT_MATCH.order = 0;
    Set<Attribute<?>> customRankingAttrs; // type of attribute should be either Boolean or Double
    Set<String> searchableAttrs; // List of attribute names. example: "title"
    List<Promotion> promotions;
    boolean removeDuplicates = false; // default == false
    int duplicateTolerance = 1;
    String attributeNameToComputeDuplicates; // example: "title"
    boolean debugMode = false; // default == false
    int distanceOfWordsInDifferentFields = 8; // default == 8, minimum = 8
    Map<QueryType, Query> queries;
    List<Doc> docs;
}
