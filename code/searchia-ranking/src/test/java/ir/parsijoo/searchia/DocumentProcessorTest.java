package ir.parsijoo.searchia;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DocumentProcessorTest {

    Path samplesPath = Path.of("src/test/resources/sample-docs.txt");

    String query;
    List<Doc> docs;
    List<Promotion> promotions;
    RankConfiguration configuration;

    @BeforeEach
    void setUp() throws IOException {
        query = "dodge charger";

        docs = Files
                .lines(samplesPath)
                .skip(1)
                .map(line -> {
                    String[] attrs = line.split("\\|");
                    int id = Integer.parseInt(attrs[0].split("=")[1]);
                    long creationDate = Long.parseLong(attrs[1].split("=")[1]);
                    long viewCount = Long.parseLong(attrs[2].split("=")[1]);
                    double score = Math.random();
                    Attribute<String> title = new Attribute<>(attrs[3].split("=")[0],
                            attrs[3].split("=")[1]);
                    Attribute<String> description = new Attribute<>(attrs[4].split("=")[0],
                            attrs[4].split("=")[1]);
                    List<Attribute<String>> searchableAttrs = List.of(title, description);
                    Map<String, Long> customAttrs = Map.of("viewCount", viewCount, "creationDate"
                            , creationDate);
                    return new Doc(id, customAttrs, score, searchableAttrs);
                })
                .collect(Collectors.toList());

        promotions = List.of(
                new Promotion(),
                new Promotion()
        );

        configuration = new RankConfiguration(
                "price",
                null,
                false,
                List.of("viewCount", "creationDate"),
                Set.of()
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void processDocs() throws IOException {
        int initialSize = docs.size();

        DocumentProcessor.processDocs(docs);

        assertThat(docs.size(), is(equalTo(initialSize)));
        assertThat(docs.get(0).getTokens(), is(notNullValue()));
    }

    @Test
    void processDoc() throws IOException {
        Doc doc = new Doc(1, null, 0, List.of(
                new Attribute<>("title", "dodge charger"),
                new Attribute<>("description", "new red dodge charger*"))
        );
        Set<String> expectedTokenStrings = Set.of("dodge", "charger", "new", "red");

        Doc result = DocumentProcessor.processDoc(doc);

        assertThat(result.getTokens().size(), is(equalTo(4)));
        assertThat(result.getTokens().keySet(), is(equalTo(expectedTokenStrings)));
    }

    @Test
    void processDoc_oneWordRepeatedInMultipleAttributes() throws IOException {
        Doc doc = docs.stream().filter(d -> d.getId() == 1).findFirst().get();
        String targetToken = "charger";

        DocumentProcessor.processDoc(doc);
        List<Integer> expectedTokenPositions = doc.getTokens().get(targetToken).getPositions();

        assertThat(expectedTokenPositions, is(equalTo(List.of(1, 1_000_004))));
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
    @ValueSource(strings = {"hello world*", "hello.world*", "hello world*", "hello,world*"})
    void tokenizeText_english_withWildcard(String text) {
        List<String> tokens = DocumentProcessor.tokenizeText(text);

        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world*", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"سلام دنیا", "سلام‌دنیا", "سلام، دنیا", "سلام، دنیا."})
    void tokenizeText_farsi(String text) {
        List<String> tokens = DocumentProcessor.tokenizeText(text);

        assertEquals(2, tokens.size());
        assertEquals("سلام", tokens.get(0));
        assertEquals("دنیا", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"سلام دنیا*", "سلام‌دنیا*", "سلام، دنیا*", "سلام، دنیا*"})
    void tokenizeText_farsi_withWildcard(String text) {
        List<String> tokens = DocumentProcessor.tokenizeText(text);

        assertEquals(2, tokens.size());
        assertEquals("سلام", tokens.get(0));
        assertEquals("دنیا*", tokens.get(1));
    }

    @Test
    void populateTokenInfo_resultSize() {
        List<String> strings = List.of("dodge", "new", "dodge", "red", "charger");
        int attributeOffset = 0;

        Map<String, TokenInfo> tokens = DocumentProcessor.populateTokenInfo(strings, attributeOffset);

        assertEquals(4, tokens.size());
    }

    @Test
    void populateTokenInfo_tokenPositions() {
        List<String> strings = List.of("dodge", "new", "red", "dodge", "charger");
        String targetToken = "dodge";
        int attributeOffset = 1_000_000;
        List<Integer> expectedPositions = List.of(attributeOffset, attributeOffset + 3);

        Map<String, TokenInfo> tokens = DocumentProcessor.populateTokenInfo(strings, attributeOffset);

        assertThat(tokens.get(targetToken).getPositions(), is(equalTo(expectedPositions)));
    }

    @Test
    void populateTokenInfo_tokenRepeated3Times() {
        List<String> strings = List.of("dodge", "new", "red", "dodge", "charger", "dodge");
        String targetToken = "dodge";
        int attributeOffset = 1_000_000;
        List<Integer> expectedPositions = List.of(attributeOffset, attributeOffset + 3, attributeOffset + 5);

        Map<String, TokenInfo> tokens = DocumentProcessor.populateTokenInfo(strings, attributeOffset);

        assertThat(tokens.get(targetToken).getPositions(), is(equalTo(expectedPositions)));
    }

    @Test
    void normalizeText() throws IOException {
        String text = "آبي";
        List<String> expectedTokens = List.of("ابی");

        List<String> normalizedTokens = DocumentProcessor.normalizeText(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void normalizeText_english() throws IOException {
        String text = "Dodge";
        List<String> expectedTokens = List.of("dodge");

        List<String> normalizedTokens = DocumentProcessor.normalizeText(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void normalizeText_multiWordText() throws IOException {
        String text = "گل‌های آبي 1 ۲";
        List<String> expectedTokens = List.of("گل", "های", "ابی", "1", "2");

        List<String> normalizedTokens = DocumentProcessor.normalizeText(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }
}
