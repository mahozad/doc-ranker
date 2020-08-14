package ir.parsijoo.searchia.processor;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Record;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.analyzer.ParsiAnalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static ir.parsijoo.searchia.Query.QueryType.WILDCARD;

public class RecordProcessor {

    public static final int ATTRIBUTES_DISTANCE = 1_000_000;
    private static final ParsiAnalyzer parsiAnalyzer = new ParsiAnalyzer();

    public static void processRecords(List<Record> records) {
        records.parallelStream().forEach(record -> {
            try {
                processRecord(record);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static Record processRecord(Record record) throws IOException {
        int offset = 0;
        for (String attr : record.getSearchableAttrs().values()) {
            Map<String, List<Integer>> tokens = tokenizeTextWithPosition(attr, offset);
            for (Entry<String, List<Integer>> token : tokens.entrySet()) {
                record.getTokens().merge(token.getKey(), token.getValue(), (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                });
            }
            offset += ATTRIBUTES_DISTANCE;
        }
        return record;
    }

    public static List<String> tokenizeTextWithoutAddingPositions(String text) throws IOException {
        TokenStream tokenStream = parsiAnalyzer.tokenStream(null, text);
        tokenStream.reset();

        List<String> normalizedTokens = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            String attribute = tokenStream.getAttribute(CharTermAttribute.class).toString();
            normalizedTokens.add(attribute);
        }

        tokenStream.close();
        return normalizedTokens;
    }

    public static Map<String, List<Integer>> tokenizeTextWithPosition(String text, int offset) throws IOException {
        Map<String, List<Integer>> tokensMap = new HashMap<>();
        TokenStream tokenStream = parsiAnalyzer.tokenStream(null, text);
        tokenStream.reset();

        int i = 0;
        while (tokenStream.incrementToken()) {
            String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
            int position = offset + i;
            // instead of this if-else can also use map#merge but performance-wise it seems worse
            if (tokensMap.containsKey(token)) {
                tokensMap.get(token).add(position);
            } else {
                List<Integer> positions = new ArrayList<>();
                positions.add(position);
                tokensMap.put(token, positions);
            }
            i++;
        }

        tokenStream.close();
        return tokensMap;
    }

    public static int getNumberOfMatches(Record record, Query query) {
        List<String> qWords = query.getTokens();
        int numberOfMatches = 0;
        for (int i = 0; i < qWords.size(); i++) {
            String qWord = qWords.get(i);
            if (query.getType() == WILDCARD && i == qWords.size() - 1) {
                boolean matches = record.getTokens().keySet().stream().anyMatch(s -> s.startsWith(qWord));
                if (matches) {
                    numberOfMatches++;
                }
            } else if (record.getTokens().containsKey(qWord)) {
                numberOfMatches++;
            }
        }
        return numberOfMatches;
    }
}
