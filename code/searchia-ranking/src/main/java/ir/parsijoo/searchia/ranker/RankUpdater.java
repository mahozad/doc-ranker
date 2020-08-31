package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Record;
import ir.parsijoo.searchia.Selector;
import ir.parsijoo.searchia.config.SortDirection;

import java.util.*;

import static ir.parsijoo.searchia.config.SortDirection.ASCENDING;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class RankUpdater<T extends Comparable<T>> {

    private final List<Record> records;
    private final Selector<T> AttributeSelector;
    private final SortDirection sortDirection;
    private final Comparator<Record> comparator;
    private T previousAttributeValue;
    private int rank;

    RankUpdater(List<Record> records, Selector<T> AttributeSelector, SortDirection sortDirection) {
        this.rank = 0; // Rank starts from 0 (i.e. top record has rank of 0)
        this.records = records;
        this.AttributeSelector = AttributeSelector;
        this.sortDirection = sortDirection;
        this.comparator = createComparator();
    }

    void updateRanks() {
        SortedMap<Integer, List<Record>> groups = groupRecordsByRank();
        for (List<Record> group : groups.values()) {
            updateGroupRanks(group);
            rank++;
        }
    }

    private Comparator<Record> createComparator() {
        if (sortDirection == ASCENDING) {
            return Comparator.comparing(AttributeSelector::get);
        } else {
            return Comparator.comparing(AttributeSelector::get).reversed();
        }
    }

    private void updateGroupRanks(List<Record> group) {
        List<Record> sortedGroup = group.stream().sorted(comparator).collect(toList());
        previousAttributeValue = AttributeSelector.get(sortedGroup.get(0));
        for (Record record : sortedGroup) {
            updateRecordRank(record);
        }
    }

    private void updateRecordRank(Record record) {
        T attributeValue = AttributeSelector.get(record);
        if (attributeValue.compareTo(previousAttributeValue) != 0) {
            rank++;
            previousAttributeValue = attributeValue;
        }
        record.setRank(rank);
    }

    private SortedMap<Integer, List<Record>> groupRecordsByRank() {
        Map<Integer, List<Record>> map = records.stream().collect(groupingBy(Record::getRank));
        return new TreeMap<>(map);
    }
}
