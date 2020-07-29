import java.util.*;
import java.util.stream.Collectors;

public class OptionalWordRanker {

    public static List<Doc> rankByOptionalWords(List<Doc> docs, RankConfiguration configuration) {
        if (configuration.getQueryOptionalWords().isEmpty()) {
            return docs;
        }

        docs = docs.subList(0, 10); // only top 10 docs
        docs.forEach(doc -> doc.setPhaseScore(0)); // reset scores
        SortedMap<Integer, List<Doc>> docsByPhaseScore = groupDocsByPhaseScore(docs);

        List<Doc> result = new ArrayList<>();
        docsByPhaseScore.keySet().forEach(key -> {
            List<Doc> group = docsByPhaseScore.get(key);
            for (Doc doc : group) {
                for (String optionalWord : configuration.getQueryOptionalWords()) {
                    if (isWordInDoc(optionalWord, doc)) {
                        doc.setPhaseScore(doc.getPhaseScore() + 1);
                    }
                }
            }
            result.addAll(group.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        });

        return result;
    }

    public static SortedMap<Integer, List<Doc>> groupDocsByPhaseScore(List<Doc> docs) {
        Map<Integer, List<Doc>> map = docs.stream().collect(Collectors.groupingBy(Doc::getPhaseScore));
        TreeMap<Integer, List<Doc>> sortedMap = new TreeMap<>(Comparator.reverseOrder());
        sortedMap.putAll(map);
        return sortedMap;
    }

    public static boolean isWordInDoc(String word, Doc doc) {
        List<String> docWords = getAllDocWords(doc);
        for (String docWord : docWords) {
            int distance = TypoRanker.measureWordsDistance(word, docWord, 2);
            if (distance >= 0 && distance <= 2) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getAllDocWords(Doc doc) {
        return doc.getSearchableAttrs()
                .stream()
                .flatMap(attribute -> Arrays.stream(TypoRanker.tokenizeText(attribute.getValue())))
                .collect(Collectors.toList());
    }
}
