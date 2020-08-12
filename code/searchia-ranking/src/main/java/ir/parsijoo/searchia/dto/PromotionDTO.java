package ir.parsijoo.searchia.dto;

import java.io.Serializable;

public class PromotionDTO implements Serializable {

    private final DocDTO docDTO; // or int docId;
    private final int positionInResult;

    public PromotionDTO(DocDTO docDTO, int positionInResult) {
        this.docDTO = docDTO;
        this.positionInResult = positionInResult;
    }

    public DocDTO getDocDTO() {
        return docDTO;
    }

    public int getPositionInResult() {
        return positionInResult;
    }
}
