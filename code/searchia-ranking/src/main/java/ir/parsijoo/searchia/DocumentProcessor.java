package ir.parsijoo.searchia;

import java.util.*;
import java.util.stream.Collectors;

public class DocumentProcessor {

    public static final int ATTRIBUTES_DISTANCE = 1_000_000;

    public static void processDocs(List<Doc> docs) {
        for (Doc doc : docs) {
            processDoc(doc);
        }
    }

    public static Doc processDoc(Doc doc) {
        int offset = 0;
        for (Attribute<String> searchableAttr : doc.getSearchableAttrs()) {
            List<String> tokens = tokenizeText(searchableAttr.getValue());
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
        return Arrays.stream(text
                .split("[\\s\\u200c]")) // \u200c is zero-width non-joiner space
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

    public static String normalizeToken(String token) {
        // return ir.parsijoo.persianstemmer.NewStemmer.getStem(token);
        // return ir.parsijoo.persianstemmer.FullStemmer.getStem(token);
        // return ir.parsijoo.persianstemmer.Stemmer.stem(token);
        // return ir.parsijoo.persianstemmer.Stemmer.normalizeChars(token);
        return "";
    }
}

class TokenInfo {

    private final List<Integer> positions = new ArrayList<>();

    public List<Integer> getPositions() {
        return positions;
    }
}
