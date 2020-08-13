package ir.parsijoo.searchia.dto;

import java.io.Serializable;

// T should be comparable. For example numeric or boolean
public class AttributeDTO<T extends Comparable<T>> implements Serializable {

    private Class<T> type;
    private String name;
    private T value;
    private SortDirection sortDirection = SortDirection.DESCENDING; // default == DESCENDING

    public AttributeDTO(Class<T> type, String name, T value, SortDirection sortDirection) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.sortDirection = sortDirection;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }
}
