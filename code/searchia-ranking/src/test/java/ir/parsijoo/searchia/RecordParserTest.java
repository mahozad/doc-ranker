package ir.parsijoo.searchia;

import ir.parsijoo.searchia.model.Query;
import ir.parsijoo.searchia.model.Query.QueryType;
import ir.parsijoo.searchia.model.Record;
import ir.parsijoo.searchia.parse.QueryParser;
import ir.parsijoo.searchia.parse.RecordParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.parsijoo.searchia.parse.RecordParser.ATTRIBUTES_DISTANCE;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordParserTest {

    List<Record> records;

    @BeforeEach
    void setUp() throws IOException {
        records = TestUtil.createSampleRecords();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void parseRecords() {
        int initialSize = records.size();

        RecordParser.parseRecords(records);

        assertThat(records.size(), is(equalTo(initialSize)));
        assertThat(records.get(0).getTokens(), is(notNullValue()));
    }

    @Test
    void parseRecord() throws IOException {
        Record record = new Record(1, null, 0, Map.of(
                "title", "dodge charger",
                "description", "new red dodge charger*"
        ));
        Set<String> expectedTokenStrings = Set.of("dodge", "charger", "new", "red");

        Record result = RecordParser.parseRecord(record);

        assertThat(result.getTokens().size(), is(equalTo(4)));
        assertThat(result.getTokens().keySet(), is(equalTo(expectedTokenStrings)));
    }

    @Test
    void parseRecord_oneWordRepeatedInMultipleAttributes() throws IOException {
        Record record = records.stream().filter(d -> d.getId() == 1).findFirst().get();
        String targetToken = "charger";

        RecordParser.parseRecord(record);
        Set<Integer> expectedTokenPositions = record.getTokens().get(targetToken).stream().map(position -> position % ATTRIBUTES_DISTANCE).collect(toSet());

        assertThat(expectedTokenPositions, is(equalTo(Set.of(1, 4))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello WoRld", "hElLo.world", "hello world.", "hello,world."})
    void tokenizeText_english(String text) throws IOException {
        List<String> tokens = RecordParser.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello world*", "hello.world*", "hElLo WoRld*", "hello,world*"})
    void tokenizeText_english_withWildcard(String text) throws IOException {
        List<String> tokens = RecordParser.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"سلام دنیا", "سلآم‌دنيآ", "سلام، دنيا", "سلآم، دنیأ."})
    void tokenizeText_farsi(String text) throws IOException {
        List<String> tokens = RecordParser.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("سلام", tokens.get(0));
        assertEquals("دنیا", tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"سلام دنیا*", "سلام‌دنيآ*", "سلام، دنیأ*", "سلآم، دنیا*"})
    void tokenizeText_farsi_withWildcard(String text) throws IOException {
        List<String> tokens = RecordParser.tokenizeTextWithoutAddingPositions(text);

        assertEquals(2, tokens.size());
        assertEquals("سلام", tokens.get(0));
        assertEquals("دنیا", tokens.get(1));
    }

    @Test
    void tokenizeText_farsi_oneWord() throws IOException {
        String text = "آبي";
        List<String> expectedTokens = List.of("ابی");

        List<String> normalizedTokens = RecordParser.tokenizeTextWithoutAddingPositions(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void tokenizeText_english_oneWord() throws IOException {
        String text = "Dodge";
        List<String> expectedTokens = List.of("dodge");

        List<String> normalizedTokens = RecordParser.tokenizeTextWithoutAddingPositions(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void tokenizeText_multiWordText() throws IOException {
        String text = "گل‌های آبي 1 ۲";
        List<String> expectedTokens = List.of("گل", "های", "ابی", "1", "2");

        List<String> normalizedTokens = RecordParser.tokenizeTextWithoutAddingPositions(text);

        assertThat(expectedTokens, is(equalTo(normalizedTokens)));
    }

    @Test
    void tokenizeTextWithPosition() throws IOException {
        String text = "Doc is a doc that is a good document";
        int positionOffset = 0;
        Set<String> expectedTokens = Set.of("doc", "is", "a", "that", "good", "document");

        Map<String, List<Integer>> tokens = RecordParser.tokenizeTextWithPosition(text, positionOffset);

        assertThat(tokens.keySet(), is(equalTo(expectedTokens)));
        assertThat(tokens.get("doc"), is(equalTo(List.of(positionOffset, 3 + positionOffset))));
        assertThat(tokens.get("that"), is(equalTo(List.of(4 + positionOffset))));
    }

    @Test
    void tokenizeTextWithPosition_offsetIsNotZero() throws IOException {
        String text = "Doc is a doc that is a good document";
        int positionOffset = 1_000_000;
        Set<String> expectedTokens = Set.of("doc", "is", "a", "that", "good", "document");

        Map<String, List<Integer>> tokens = RecordParser.tokenizeTextWithPosition(text, positionOffset);

        assertThat(tokens.keySet(), is(equalTo(expectedTokens)));
        assertThat(tokens.get("doc"), is(equalTo(List.of(positionOffset, 3 + positionOffset))));
        assertThat(tokens.get("that"), is(equalTo(List.of(4 + positionOffset))));
    }

    @Test
    void isRecordMatchingWithQuery() throws IOException {
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        Record record = records.get(1);
        record.setTokens(Map.of("dodge", List.of(), "charter", List.of()));
        QueryParser.parseQueries(Map.of(QueryType.ORIGINAL, query));

        boolean isMatching = RecordParser.isRecordMatchedWithQuery(record, query);

        assertTrue(isMatching);
    }

    @Test
    void isRecordMatchingWithQuery_wildcardQuery() throws IOException {
        Query query = new Query("dodge charter*", QueryType.WILDCARD);
        Record record = records.get(1);
        record.setTokens(Map.of("dodge", List.of(), "charter", List.of()));
        QueryParser.parseQueries(Map.of(QueryType.WILDCARD, query));

        boolean isMatching = RecordParser.isRecordMatchedWithQuery(record, query);

        assertTrue(isMatching);
    }

    @Test
    void isRecordMatchingWithQuery_wildcardQuery_withoutAsteriskAtEnd() throws IOException {
        Query query = new Query("dodge charter", QueryType.WILDCARD);
        Record record = records.get(1);
        record.setTokens(Map.of("dodge", List.of(), "charter", List.of()));
        QueryParser.parseQueries(Map.of(QueryType.WILDCARD, query));

        boolean isMatching = RecordParser.isRecordMatchedWithQuery(record, query);

        assertTrue(isMatching);
    }

    @Test
    void getNumberOfMatches() throws IOException {
        Record record = records.stream().filter(d -> d.getId() == 1).findFirst().get();
        Query query = new Query("dodge charter", QueryType.ORIGINAL);
        QueryParser.parseQueries(Map.of(QueryType.ORIGINAL, query));
        RecordParser.parseRecord(record);

        int numberOfMatches = RecordParser.getNumberOfMatches(record, query);

        assertEquals(1, numberOfMatches);
    }

    @Test
    void getNumberOfMatches_wildCardQuery() throws IOException {
        Record record = records.stream().filter(d -> d.getId() == 8).findFirst().get();
        Query query = new Query("lamborghini aventado*", QueryType.WILDCARD);
        QueryParser.parseQueries(Map.of(QueryType.WILDCARD, query));
        RecordParser.parseRecord(record);

        int numberOfMatches = RecordParser.getNumberOfMatches(record, query);

        assertEquals(2, numberOfMatches);
    }
}