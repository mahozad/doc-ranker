import java.util.*;

public class DocumentProcessor {

    public static Set<Token> extractQueryTokensFromText(String query, String text) {
        Set<Token> tokens = new HashSet<>();
        // Map<String, Set<Token>> tokens = new HashMap<>();
        String[] queryTokens = TypoRanker.tokenizeText(query);
        String[] textTokens = TypoRanker.tokenizeText(text);

        int position = 0;
        for (String textToken : textTokens) {
            for (String queryToken : queryTokens) {
                int distance = TypoRanker.measureWordsDistance(textToken, queryToken, 2);
                if (distance >= 0 && distance <= 2) {
                    Token token = new Token();
                    token.setValue(textToken);
                    token.setCorrespondingQueryToken(queryToken);
                    token.setNumberOfTypos(distance);
                    token.setPosition(position);
                    tokens.add(token);
                }
            }
            position++;
        }

        return tokens;
    }
}

class Token {

    private String value;
    private String correspondingQueryToken;
    private int numberOfTypos;
    private int position;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCorrespondingQueryToken() {
        return correspondingQueryToken;
    }

    public void setCorrespondingQueryToken(String correspondingQueryToken) {
        this.correspondingQueryToken = correspondingQueryToken;
    }

    public int getNumberOfTypos() {
        return numberOfTypos;
    }

    public void setNumberOfTypos(int numberOfTypos) {
        this.numberOfTypos = numberOfTypos;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return numberOfTypos == token.numberOfTypos &&
                value.equals(token.value) &&
                correspondingQueryToken.equals(token.correspondingQueryToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, correspondingQueryToken, numberOfTypos);
    }
}
