package ir.parsijoo.searchia;

import ir.parsijoo.searchia.processor.DocumentProcessor;
import ir.parsijoo.searchia.processor.QueryProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.processor.DocumentProcessor.ATTRIBUTES_DISTANCE;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentProcessorTest {

    List<Doc> docs;

    @BeforeEach
    void setUp() throws IOException {
        docs = TestUtil.createSampleDocs();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void processDocs() {
        int initialSize = docs.size();

        DocumentProcessor.processDocs(docs);

        assertThat(docs.size(), is(equalTo(initialSize)));
        assertThat(docs.get(0).getTokens(), is(notNullValue()));
    }

    @Test
    void processDoc() throws IOException {
        Doc doc = new Doc(1, null, 0, Map.of(
                "title", "dodge charger",
                "description", "new red dodge charger*"
        ));
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
        Set<Integer> expectedTokenPositions = doc.getTokens().get(targetToken).stream().map(position -> position % ATTRIBUTES_DISTANCE).collect(toSet());

        assertThat(expectedTokenPositions, is(equalTo(Set.of(1, 4))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello WoRld", "hElLo.world", "hello world.", "hello,world."})
    void tokenizeText_english(String text) throws IOException {
        List<String> tokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello world*", "hello.world*", "hElLo WoRld*", "hello,world*"})
    void tokenizeText_english_withWildcard(String text) throws IOException {
        List<String> tokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"سلام دنیا", "سلآم‌دنيآ", "سلام، دنيا", "سلآم، دنیأ."})
    void tokenizeText_farsi(String text) throws IOException {
        List<String> tokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("سلام", tokens.get(0));
        assertEquals("دنیا", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"سلام دنیا*", "سلام‌دنيآ*", "سلام، دنیأ*", "سلآم، دنیا*"})
    void tokenizeText_farsi_withWildcard(String text) throws IOException {
        List<String> tokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("سلام", tokens.get(0));
        assertEquals("دنیا", tokens.get(1));
    }

    @Test
    void tokenizeText_farsi_oneWord() throws IOException {
        String text = "آبي";
        List<String> expectedTokens = List.of("ابی");

        List<String> normalizedTokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void tokenizeText_english_oneWord() throws IOException {
        String text = "Dodge";
        List<String> expectedTokens = List.of("dodge");

        List<String> normalizedTokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void tokenizeText_multiWordText() throws IOException {
        String text = "گل‌های آبي 1 ۲";
        List<String> expectedTokens = List.of("گل", "های", "ابی", "1", "2");

        List<String> normalizedTokens = DocumentProcessor.tokenizeTextWithoutAddingPositions(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void tokenizeTextWithPosition() throws IOException {
        String text = "Doc is a doc that is a good document";
        int positionOffset = 0;
        Set<String> expectedTokens = Set.of("doc", "is", "a", "that", "good", "document");

        Map<String, List<Integer>> tokens = DocumentProcessor.tokenizeTextWithPosition(text, positionOffset);

        assertThat(tokens.keySet(), is(equalTo(expectedTokens)));
        assertThat(tokens.get("doc"), is(equalTo(List.of(positionOffset, 3 + positionOffset))));
        assertThat(tokens.get("that"), is(equalTo(List.of(4 + positionOffset))));
    }

    @Test
    void tokenizeTextWithPosition_offsetIsNotZero() throws IOException {
        String text = "Doc is a doc that is a good document";
        int positionOffset = 1_000_000;
        Set<String> expectedTokens = Set.of("doc", "is", "a", "that", "good", "document");

        Map<String, List<Integer>> tokens = DocumentProcessor.tokenizeTextWithPosition(text, positionOffset);

        assertThat(tokens.keySet(), is(equalTo(expectedTokens)));
        assertThat(tokens.get("doc"), is(equalTo(List.of(positionOffset, 3 + positionOffset))));
        assertThat(tokens.get("that"), is(equalTo(List.of(4 + positionOffset))));
    }

    @Test
    void getNumberOfMatches() throws IOException {
        Doc doc = docs.stream().filter(d -> d.getId() == 1).findFirst().get();
        Query query = new Query("dodge charter", Query.QueryType.ORIGINAL);
        QueryProcessor.processQueries(Map.of(Query.QueryType.ORIGINAL, query));
        DocumentProcessor.processDoc(doc);

        int numberOfMatches = DocumentProcessor.getNumberOfMatches(doc, query);

        assertEquals(1, numberOfMatches);
    }

    @Test
    void getNumberOfMatches_wildCardQuery() throws IOException {
        Doc doc = docs.stream().filter(d -> d.getId() == 8).findFirst().get();
        Query query = new Query("lamborghini aventado*", Query.QueryType.WILDCARD);
        QueryProcessor.processQueries(Map.of(Query.QueryType.WILDCARD, query));
        DocumentProcessor.processDoc(doc);

        int numberOfMatches = DocumentProcessor.getNumberOfMatches(doc, query);

        assertEquals(2, numberOfMatches);
    }
}
