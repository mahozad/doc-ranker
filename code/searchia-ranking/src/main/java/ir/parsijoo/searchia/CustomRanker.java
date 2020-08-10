package ir.parsijoo.searchia;

import java.util.*;
import java.util.stream.Collectors;

public class CustomRanker {

    public static List<Doc> rankByCustomAttributes(List<Doc> docs, List<String> customAttrs) {
        for (String attrName : customAttrs) {
            SortedMap<Long, List<Doc>> groups = OptionalWordRanker.groupDocsByRank(docs);
            int rank = 0; // Rank starts from 0 (top doc has rank of 0)
            for (List<Doc> group : groups.values()) {
                List<Doc> sortedGroup = group.stream().sorted((d1, d2) -> {
                    Object attr1 = d1.getCustomRankingAttrs().get(attrName);
                    Object attr2 = d2.getCustomRankingAttrs().get(attrName);
                    if (attr1 instanceof Boolean && attr2 instanceof Boolean) {
                        return d2.compareTo(d1);
                    } else if (attr1 instanceof Double && attr2 instanceof Double) {
                        return ((Double) attr2).compareTo((Double) attr1);
                    } else {
                        throw new RuntimeException("The attribute \"" + attrName + "\" that is provided for custom ranking is not boolean or numeric");
                    }
                }).collect(Collectors.toList());
                Object previousAttrValue = sortedGroup.get(0).getCustomRankingAttrs().get(attrName);
                // long rank = sortedGroup.get(0).getRank();
                for (Doc doc : sortedGroup) {
                    if (!doc.getCustomRankingAttrs().get(attrName).equals(previousAttrValue)) {
                        rank++;
                        doc.setRank(rank);
                        previousAttrValue = doc.getCustomRankingAttrs().get(attrName);
                    } else {
                        doc.setRank(rank);
                    }
                }
                rank ++;
            }
        }
        docs.sort((d1, d2) -> (int) (d1.getRank() - d2.getRank()));
        return docs;
    }
}
