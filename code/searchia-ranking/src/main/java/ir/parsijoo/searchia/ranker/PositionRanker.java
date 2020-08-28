package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Query;
import ir.parsijoo.searchia.Query.QueryType;
import ir.parsijoo.searchia.RankingExecutor;
import ir.parsijoo.searchia.Record;
import ir.parsijoo.searchia.config.RankingPhase;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ir.parsijoo.searchia.parser.RecordParser.ATTRIBUTES_DISTANCE;

public class PositionRanker implements Ranker {

    @Override
    public void rank(Map<QueryType, Query> queries, List<Record> records, RankingPhase phase) {
        for (Record record : records) {
            int minPosition = Integer.MAX_VALUE;
            for (Query query : queries.values()) {
                int minPositionFromQuery = getRecordMinWordPositionByQuery(record, query);
                if (minPositionFromQuery < minPosition) {
                    minPosition = minPositionFromQuery;
                }
            }
            // FIXME: The attribute name is set to a constant value
            record.setMinPosition(new Record.MinPosition(minPosition, "title"));
        }
        RankingExecutor.updateRanks(records, record -> record.getMinPosition().value, phase.getSortDirection());
    }

    public static int getRecordMinWordPositionByQuery(Record record, Query query) {
        int minPosition = Integer.MAX_VALUE;
        for (String qWord : query.getTokens()) {
            if (record.getTokens().containsKey(qWord)) {
                List<Integer> tokenPositions = record.getTokens().get(qWord);
                Optional<Integer> min = tokenPositions.stream().min(Comparator.comparingInt(p -> p % ATTRIBUTES_DISTANCE));
                if (min.isPresent() && min.get() < minPosition) {
                    minPosition = min.get() % ATTRIBUTES_DISTANCE;
                }
            }
        }
        return minPosition;
    }
}
