package searchia;

import java.util.*;
import java.util.stream.Collectors;

public class DocumentProcessor {

    public static void processDocs(List<Doc> docs) {
        for (Doc doc : docs) {
            processDoc(doc);
        }
    }

    public static Doc processDoc(Doc doc) {
        for (Attribute<String> searchableAttr : doc.getSearchableAttrs()) {
            List<String> tokens = tokenizeText(searchableAttr.getValue());
            Map<String, TokenInfo> tokenInfo = populateTokenInfo(tokens);
            doc.getTokens().putAll(tokenInfo);
        }
        return doc;
    }

    public static List<String> tokenizeText(String text) {
        return Arrays.stream(text
                .split("[\\s\\u200c]")) // \u200c is zero-width non-joiner space
                .flatMap(token -> Arrays.stream(token.split("[.,;:\"،؛']")))
                .collect(Collectors.toList());
    }

    public static Map<String, TokenInfo> populateTokenInfo(List<String> tokens) {
        Map<String, TokenInfo> tokensMap = new HashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.getPositions().add(i);
            tokensMap.merge(token, tokenInfo, (oldInfo, newInfo) -> {
                oldInfo.getPositions().addAll(newInfo.getPositions());
                return oldInfo;
            });
        }
        return tokensMap;
    }
}

class TokenInfo {

    private final List<Integer> positions = new ArrayList<>();

    public List<Integer> getPositions() {
        return positions;
    }
}