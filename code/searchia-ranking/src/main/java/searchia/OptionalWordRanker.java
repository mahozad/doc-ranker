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
        Query originalQuery = queries.get(QueryType.ORIGINAL);
        int lengthOfOriginalQuery = DocumentProcessor.tokenizeText(originalQuery.getText()).size();
        if (!queries.containsKey(QueryType.OPTIONAL)) {
            for (Doc doc : docs) {
                doc.setNumberOfMatches(lengthOfOriginalQuery);
            }
            return docs;
        } else {
            Query optionalQuery = queries.get(QueryType.OPTIONAL);
            int lengthOfOptionalQuery = DocumentProcessor.tokenizeText(optionalQuery.getText()).size();
            List<Doc> result = new ArrayList<>();
            SortedMap<Long, List<Doc>> docGroups = groupDocsByRank(docs);

            for (List<Doc> group : docGroups.values()) {
                for (Doc doc : group) {
                    for (Query query : queries.values().stream().filter(query -> query.getType() != QueryType.OPTIONAL).collect(Collectors.toList())) {
                        if (TypoRanker.isDocMatchedWithQuery(doc, query)) {
                            doc.setNumberOfMatches(lengthOfOriginalQuery);
                            break;
                        }
                    }
                    doc.setNumberOfMatches(Math.max(doc.getNumberOfMatches(), lengthOfOptionalQuery));
                }

                List<Doc> sortedGroup = group.stream().sorted((doc1, doc2) -> doc2.getNumberOfMatches() - doc1.getNumberOfMatches()).collect(Collectors.toList());
                long previousNumberOfMatches = sortedGroup.get(0).getNumberOfMatches();
                long rank = sortedGroup.get(0).getRank();
                for (Doc doc : sortedGroup) {
                    if (doc.getNumberOfMatches() != previousNumberOfMatches) {
                        rank++;
                        doc.setRank(rank);
                        previousNumberOfMatches = doc.getNumberOfMatches();
                    } else {
                        doc.setRank(rank);
                    }
                }

                result.addAll(sortedGroup);
            }
            return result;
        }
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
