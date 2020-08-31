package ir.parsijoo.searchia;

import ir.parsijoo.searchia.model.Record;

@FunctionalInterface
public interface Selector<T> {
    T get(Record record);
}
