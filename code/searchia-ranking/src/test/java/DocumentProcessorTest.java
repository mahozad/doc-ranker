import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentProcessorTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void extractQueryTokensFromText() {
        String query = "dodge charger";
        String text = "dodge new red charger";
        Token expectedToken = new Token();
        expectedToken.setValue("dodge");
        expectedToken.setNumberOfTypos(0);
        expectedToken.setCorrespondingQueryToken("dodge");

        Set<Token> tokens = DocumentProcessor.extractQueryTokensFromText(query, text);

        assertTrue(tokens.contains(expectedToken));
    }
}
