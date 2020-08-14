package ir.parsijoo.searchia.ranker;

import ir.parsijoo.searchia.Record;

import java.util.List;
import java.util.Set;

public class FilterRanker {

    public static List<Record> rankByFilters(List<Record> records, Set<Filter<?>> filters) {
//        filters.forEach(filter -> {
//            for (Record record : records) {
                // if (filter.getValue() instanceof Double) {
//                int score = setRecordScoreByNumericFilter(record, filter);
//                record.setPhaseScore(record.getPhaseScore() + score);
                // }
//            }
//        });

        return records;
    }

    public static <T> int setRecordScoreByNumericFilter(Record record, Filter<T> filter) {
        String attributeName = filter.getAttributeName();
        T attributeValue = ((T) record.getFilterableAttrs().get(attributeName));

        if (attributeValue == null) {
            return 0;
        } else if (filter.isFilterSatisfied(attributeValue)) {
            return 1;
        } else {
            return 0;
        }
    }
    public enum Operator {
        LT, LTE, EQ, GT, GTE
    }
    public static class Filter<T> {

        private String attributeName;
        private T value;
        private Operator operator;
        private int weight;

        public boolean isFilterSatisfied(T value) {
            if (value instanceof String) {
                return value == this.value;
            } else if (operator == Operator.LT) {
                return ((double) value) < ((double) this.value);
            } else if (operator == Operator.LTE) {
                return ((double) value) <= ((double) this.value);
            } else if (operator == Operator.EQ) {
                return ((double) value) == ((double) this.value);
            } else if (operator == Operator.GTE) {
                return ((double) value) >= ((double) this.value);
            } else {
                return ((double) value) > ((double) this.value);
            }
        }

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
