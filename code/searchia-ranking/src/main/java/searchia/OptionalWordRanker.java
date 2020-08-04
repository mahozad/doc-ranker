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
    public static List<Doc> rankByOptionalWords(Map<QueryType, Query> queries, List<Doc> docs) {
        if (!queries.containsKey(QueryType.OPTIONAL)) {
            int numberOfWords = 0;
            Query originalQuery = queries.get(QueryType.ORIGINAL);
            numberOfWords = DocumentProcessor.tokenizeText(originalQuery.getText()).size();
            for (Doc doc : docs) {
                doc.setNumberOfMatches(numberOfWords);
            }
            return docs;
        }
        return null;
    }

    public static SortedMap<Long, List<Doc>> groupDocsByRank(List<Doc> docs) {
        Map<Long, List<Doc>> map = docs.stream().collect(Collectors.groupingBy(Doc::getRank));
        return new TreeMap<>(map);
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
