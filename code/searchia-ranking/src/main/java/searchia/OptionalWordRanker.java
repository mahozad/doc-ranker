package searchia;

import searchia.Query.QueryType;

import java.util.*;
import java.util.stream.Collectors;

public class OptionalWordRanker {

    /**
     * If we do not have optional query then number of matches in all the docs is same and is equal
     * to number of words of the original query even if there is a longer query and a doc
     * has matched all its words.
     *
     * @param queries
     * @param docs
     * @return
     */
    public static List<Doc> rankByOptionalWords(List<Query> queries, List<Doc> docs) {
        Set<QueryType> queryTypes = queries.stream().map(Query::getType).collect(Collectors.toSet());
        if (!queryTypes.contains(QueryType.OPTIONAL)) {
            int numberOfWords = 0;
            for (Query query : queries) {
                if (query.getType() == QueryType.ORIGINAL) {
                    numberOfWords = DocumentProcessor.tokenizeText(query.getText()).size();
                }
            }
            for (Doc doc : docs) {
                doc.setNumberOfMatches(numberOfWords);
            }
            return docs;
        }
        return null;
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
                .flatMap(attribute -> DocumentProcessor.tokenizeText(attribute.getValue()).stream())
                .collect(Collectors.toList());
    }
}
