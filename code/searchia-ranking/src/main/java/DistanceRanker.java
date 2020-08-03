import java.util.List;

public class DistanceRanker {

    public static List<Doc> rankByWordsDistance(String query, List<Doc> docs) {
        String[] qWords = TypoRanker.tokenizeText(query);
        if (qWords.length < 2) {
            return docs;
        }

        return List.of(new Doc(1, null, 0, null));
    }

    public static int[] getWordPositions(String word, Attribute<String> attribute) {
        return null;
    }
}
