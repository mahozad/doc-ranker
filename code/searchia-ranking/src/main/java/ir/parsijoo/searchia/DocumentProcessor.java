package ir.parsijoo.searchia;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.analyzer.ParsiAnalyzer;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static ir.parsijoo.searchia.Query.QueryType.WILDCARD;

public class DocumentProcessor {

    public static final int ATTRIBUTES_DISTANCE = 1_000_000;

    public static void processDocs(List<Doc> docs) throws IOException {
        for (Doc doc : docs) {
            processDoc(doc);
        }
    }

    public static Doc processDoc(Doc doc) throws IOException {
        int offset = 0;
        for (Attribute<String> searchableAttr : doc.getSearchableAttrs()) {
            List<String> tokens = normalizeText(searchableAttr.getValue());
            Map<String, TokenInfo> tokensMap = populateTokenInfo(tokens, offset);
            for (Entry<String, TokenInfo> token : tokensMap.entrySet()) {
                doc.getTokens().merge(token.getKey(), token.getValue(), (v1, v2) -> {
                    v1.getPositions().addAll(v2.getPositions());
                    return v1;
                });
            }
            offset += ATTRIBUTES_DISTANCE;
        }
        return doc;
    }

    public static List<String> tokenizeText(String text) {
        return Arrays.stream(text.split("[\\s\\u200c]")) // \u200c is zero-width non-joiner space
                .flatMap(token -> Arrays.stream(token.split("[.,;:\"،؛']")))
                .peek(token -> {
                    // String str = "گل‌های آبي 1 ۲";
                    // ir.parsijoo.persianstemmer.Stemmer.stem()
                    //
                    // ParsiAnalyzer parsiAnalyzer = new ParsiAnalyzer();
                    //
                    // TokenStream tokenStream = parsiAnalyzer.tokenStream(null,str);
                    // tokenStream.reset();
                    // while (tokenStream.incrementToken()) {
                    //     Attribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
                    //     String string = attribute.toString();
                    //     System.out.println(string);
                    // }
                })
                .collect(Collectors.toList());
    }

    /**
     * Positions of a word for every other attribute starts with a large offset.
     *
     * @param tokens
     * @param positionOffset
     * @return
     */
    public static Map<String, TokenInfo> populateTokenInfo(List<String> tokens, int positionOffset) {
        Map<String, TokenInfo> tokensMap = new HashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            TokenInfo tokenInfo = new TokenInfo();
            int position = positionOffset + i;
            tokenInfo.getPositions().add(position);
            tokensMap.merge(token, tokenInfo, (oldInfo, newInfo) -> {
                oldInfo.getPositions().addAll(newInfo.getPositions());
                return oldInfo;
            });
        }
        return tokensMap;
    }

    public static List<String> normalizeText(String text) throws IOException {
        // TODO: Extract this object creation
        ParsiAnalyzer parsiAnalyzer = new ParsiAnalyzer();
        TokenStream tokenStream = parsiAnalyzer.tokenStream(null, text);
        tokenStream.reset();

        List<String> normalizedTokens = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
            normalizedTokens.add(attribute.toString());
        }

        return normalizedTokens;
    }

    public static int getNumberOfMatches(Doc doc, Query query) throws IOException {
        List<String> qWords = normalizeText(query.getText());
        int numberOfMatches = 0;
        for (int i = 0; i < qWords.size(); i++) {
            if (query.getType() == WILDCARD && i == qWords.size() - 1) {
                String qWordStem = qWords.get(i).replace("*", "");
                boolean matches = doc.getTokens().keySet().stream().anyMatch(s -> s.startsWith(qWordStem));
                if (matches) {
                    numberOfMatches++;
                }
            } else if (doc.getTokens().containsKey(qWords.get(i))) {
                numberOfMatches++;
            }
        }
        return numberOfMatches;
    }
}

class TokenInfo {

    private final List<Integer> positions = new ArrayList<>();

    public List<Integer> getPositions() {
        return positions;
    }
}
