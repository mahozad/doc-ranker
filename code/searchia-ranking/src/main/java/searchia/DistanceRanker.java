package searchia;

import searchia.Query.QueryType;

import java.util.List;
import java.util.Map;

public class DistanceRanker {

    public static List<Doc> rankByWordsDistance(String query, List<Doc> docs) {
        List<String> qWords = DocumentProcessor.tokenizeText(query);
        if (qWords.size() < 2) {
            return docs;
        }

        return List.of(new Doc(1, null, 0, null));
    }

    public static int calculateDocDistanceByQuery(Doc doc, Query query) {
        return 4;
    }
}
