package ir.parsijoo.searchia;

import java.util.List;
import java.util.function.Function;

public class CustomRanker {

    public static void rankByCustomAttributes(List<Doc> docs, List<String> customAttrs) {
        for (String attrName : customAttrs) {
            Object attr = docs.get(0).getCustomRankingAttrs().get(attrName);
            if (attr instanceof Boolean) {
                Function<Doc, Boolean> function = doc -> (Boolean) doc.getCustomRankingAttrs().get(attrName);
                Ranker.updateRanks(docs, function, true);
            } else if (attr instanceof Double) {
                Function<Doc, Double> function = doc -> (Double) doc.getCustomRankingAttrs().get(attrName);
                Ranker.updateRanks(docs, function, true);
            } else {
                throw new RuntimeException("The attribute \"" + attrName + "\" provided for custom ranking is not of type Boolean or Double");
            }
        }
    }
}
