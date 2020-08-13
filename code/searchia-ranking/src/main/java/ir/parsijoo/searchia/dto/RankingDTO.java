package ir.parsijoo.searchia.dto;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RankingDTO implements Serializable {

    private EnumMap<RankingPhaseDTO, Integer> phaseOrders = new EnumMap<>(RankingPhaseDTO.class); // phases.put(TYPO, 0); phases.put(...
    private List<DocDTO> docs;
    private Map<QueryDTO.QueryTypeDTO, QueryDTO> queries;
    private Map<String, SortDirectionDTO> customRankingAttrs; // Map from attribute name to its sort direction. Example: "viewCount" -> ASCENDING
    private Set<String> searchableAttrs; // Set of attribute names. Example: "title"
    private List<PromotionDTO> promotions;
    private boolean typoPhaseEnabled = true; // default == true
    private boolean removeDuplicates = false; // default == false
    private int duplicateTolerance = 1;
    private String attributeNameToComputeDuplicates; // example: "title"
    private boolean debugMode = false; // default == false
    private int distanceOfWordsInDifferentFields = 8; // default == 8, minimum = 8

    public boolean isTypoPhaseEnabled() {
        return typoPhaseEnabled;
    }

    public void setTypoPhaseEnabled(boolean typoPhaseEnabled) {
        this.typoPhaseEnabled = typoPhaseEnabled;
    }

    public EnumMap<RankingPhaseDTO, Integer> getPhaseOrders() {
        return phaseOrders;
    }

    public void setPhaseOrders(EnumMap<RankingPhaseDTO, Integer> phaseOrders) {
        this.phaseOrders = phaseOrders;
    }

    public Map<String, SortDirectionDTO> getCustomRankingAttrs() {
        return customRankingAttrs;
    }

    public void setCustomRankingAttrs(Map<String, SortDirectionDTO> customRankingAttrs) {
        this.customRankingAttrs = customRankingAttrs;
    }

    public Set<String> getSearchableAttrs() {
        return searchableAttrs;
    }

    public void setSearchableAttrs(Set<String> searchableAttrs) {
        this.searchableAttrs = searchableAttrs;
    }

    public List<PromotionDTO> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<PromotionDTO> promotions) {
        this.promotions = promotions;
    }

    public boolean isRemoveDuplicates() {
        return removeDuplicates;
    }

    public void setRemoveDuplicates(boolean removeDuplicates) {
        this.removeDuplicates = removeDuplicates;
    }

    public int getDuplicateTolerance() {
        return duplicateTolerance;
    }

    public void setDuplicateTolerance(int duplicateTolerance) {
        this.duplicateTolerance = duplicateTolerance;
    }

    public String getAttributeNameToComputeDuplicates() {
        return attributeNameToComputeDuplicates;
    }

    public void setAttributeNameToComputeDuplicates(String attributeNameToComputeDuplicates) {
        this.attributeNameToComputeDuplicates = attributeNameToComputeDuplicates;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getDistanceOfWordsInDifferentFields() {
        return distanceOfWordsInDifferentFields;
    }

    public void setDistanceOfWordsInDifferentFields(int distanceOfWordsInDifferentFields) {
        this.distanceOfWordsInDifferentFields = distanceOfWordsInDifferentFields;
    }

    public Map<QueryDTO.QueryTypeDTO, QueryDTO> getQueries() {
        return queries;
    }

    public void setQueries(Map<QueryDTO.QueryTypeDTO, QueryDTO> queries) {
        this.queries = queries;
    }

    public List<DocDTO> getDocs() {
        return docs;
    }

    public void setDocs(List<DocDTO> docs) {
        this.docs = docs;
    }
}
