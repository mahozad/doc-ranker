package ir.parsijoo.searchia;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.analyzer.ParsiAnalyzer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
            Map<String, TokenInfo> tokenInfo = populateTokenInfo(tokens, offset);
            tokenInfo.forEach((k, v) -> doc.getTokens().merge(k, v, (t1, t2) -> {
                t1.getPositions().addAll(t2.getPositions());
                return t1;
            }));
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
     * Positions of a word for every attribute has a large offset.
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
}

class TokenInfo {

    private final List<Integer> positions = new ArrayList<>();

    public List<Integer> getPositions() {
        return positions;
    }
}
