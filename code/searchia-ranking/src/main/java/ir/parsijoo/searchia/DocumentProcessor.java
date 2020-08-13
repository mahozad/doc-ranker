package ir.parsijoo.searchia;

import ir.parsijoo.searchia.Query.QueryType;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.analyzer.ParsiAnalyzer;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static ir.parsijoo.searchia.Query.QueryType.WILDCARD;

public class DocumentProcessor {

    public static final int ATTRIBUTES_DISTANCE = 1_000_000;
    private static final ParsiAnalyzer parsiAnalyzer = new ParsiAnalyzer();
    private static String wildcardToken;
    private static String wildcardTokenStem;

    public static void processDocs(List<Doc> docs, Map<String, Set<QueryType>> tokenToQueryTypes) {
        wildcardToken = tokenToQueryTypes.keySet().stream().filter(token -> token.endsWith("*")).findFirst().orElse("");
        wildcardTokenStem = wildcardToken.replace("*", "");
        docs.parallelStream().forEach(doc -> {
            try {
                processDoc(doc, tokenToQueryTypes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static Doc processDoc(Doc doc, Map<String, Set<QueryType>> tokenToQueryTypes) throws IOException {
        int offset = 0;
        for (Attribute<String> attr : doc.getSearchableAttrs()) {
            Map<String, List<Integer>> tokens = tokenizeTextWithPosition(doc, attr.getValue(), offset, tokenToQueryTypes);
            for (Entry<String, List<Integer>> token : tokens.entrySet()) {
                doc.getTokens().merge(token.getKey(), token.getValue(), (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                });
            }
            offset += ATTRIBUTES_DISTANCE;
        }
        return doc;
    }

    public static List<String> tokenizeTextWithoutAddingPositions(String text) throws IOException {
        TokenStream tokenStream = parsiAnalyzer.tokenStream(null, text);
        tokenStream.reset();

        List<String> normalizedTokens = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            String attribute = tokenStream.getAttribute(CharTermAttribute.class).toString();
            normalizedTokens.add(attribute);
        }

        tokenStream.close();
        return normalizedTokens;
    }

    public static Map<String, List<Integer>> tokenizeTextWithPosition(Doc doc,
                                                                      String text,
                                                                      int offset,
                                                                      Map<String, Set<QueryType>> tokenToQueryTypes) throws IOException {
        Map<String, List<Integer>> tokensMap = new HashMap<>();
        TokenStream tokenStream = parsiAnalyzer.tokenStream(null, text);
        tokenStream.reset();
        int i = 0;

        while (tokenStream.incrementToken()) {
            String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
            int position = offset + i;
            if (tokenToQueryTypes.containsKey(token)) {
                Set<QueryType> queryTypes = tokenToQueryTypes.get(token);
                queryTypes.forEach(queryType -> doc.getQueryToNumberOfMatches().merge(queryType, 1, Integer::sum));

                // can also use map#merge instead of this if-else but performance-wise it may be worse
                if (tokensMap.containsKey(token)) {
                    tokensMap.get(token).add(position);
                } else {
                    List<Integer> positions = new ArrayList<>();
                    positions.add(position);
                    tokensMap.put(token, positions);
                }
            } else if (!wildcardToken.isEmpty() && token.startsWith(wildcardTokenStem)) {
                doc.getQueryToNumberOfMatches().merge(WILDCARD, 1, Integer::sum);

                // can also use map#merge instead of this if-else but performance-wise it may be worse
                if (tokensMap.containsKey(wildcardToken)) {
                    tokensMap.get(wildcardToken).add(position);
                } else {
                    List<Integer> positions = new ArrayList<>();
                    positions.add(position);
                    tokensMap.put(wildcardToken, positions);
                }
            }
            i++;
        }

        tokenStream.close();
        return tokensMap;
    }

    public static int getNumberOfMatches(Doc doc, Query query) {
        List<String> qWords = query.getTokens();
        int numberOfMatches = 0;
        for (int i = 0; i < qWords.size(); i++) {
            String qWord = qWords.get(i);
            if (query.getType() == WILDCARD && i == qWords.size() - 1) {
                boolean matches = doc.getTokens().keySet().stream().anyMatch(s -> s.startsWith(qWord));
                if (matches) {
                    numberOfMatches++;
                }
            } else if (doc.getTokens().containsKey(qWord)) {
                numberOfMatches++;
            }
        }
        return numberOfMatches;
    }
}
