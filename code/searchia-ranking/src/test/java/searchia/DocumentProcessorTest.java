package searchia;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import searchia.DocumentProcessor;
import searchia.TokenInfo;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DocumentProcessorTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello world", "hello.world", "hello world.", "hello,world."})
    void tokenizeText_english(String text) {
        List<String> tokens = DocumentProcessor.tokenizeText(text);

        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"سلام دنیا", "سلام‌دنیا", "سلام، دنیا", "سلام، دنیا."})
    void tokenizeText_farsi(String text) {
        List<String> tokens = DocumentProcessor.tokenizeText(text);

        assertEquals(2, tokens.size());
        assertEquals("سلام", tokens.get(0));
        assertEquals("دنیا", tokens.get(1));
    }

    @Test
    void populateTokenInfo_resultSize() {
        List<String> strings = List.of("dodge", "new", "dodge", "red", "charger");

        Map<String, TokenInfo> tokens = DocumentProcessor.populateTokenInfo(strings);

        assertEquals(4, tokens.size());
    }

    @Test
    void populateTokenInfo_tokenPositions() {
        List<String> strings = List.of("dodge", "new", "red", "dodge", "charger");
        String targetToken = "dodge";
        List<Integer> expectedPositions = List.of(0, 3);

        Map<String, TokenInfo> tokens = DocumentProcessor.populateTokenInfo(strings);

        assertThat(tokens.get(targetToken).getPositions(), is(equalTo(expectedPositions)));
    }

    @Test
    void populateTokenInfo_tokenRepeated3Times() {
        List<String> strings = List.of("dodge", "new", "red", "dodge", "charger", "dodge");
        String targetToken = "dodge";
        List<Integer> expectedPositions = List.of(0, 3, 5);

        Map<String, TokenInfo> tokens = DocumentProcessor.populateTokenInfo(strings);

        assertThat(tokens.get(targetToken).getPositions(), is(equalTo(expectedPositions)));
    }
}
