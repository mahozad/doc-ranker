package ir.parsijoo.searchia;

@FunctionalInterface
public interface Selector<T> {
    T get(Record record);
}
