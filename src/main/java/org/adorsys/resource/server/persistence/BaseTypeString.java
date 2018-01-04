package org.adorsys.resource.server.persistence;

import java.io.Serializable;

/**
 * Created by peter on 20.02.17.
 */
class BaseTypeString  implements Serializable {
    private String value;

    protected BaseTypeString() {}

    protected BaseTypeString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTypeString that = (BaseTypeString) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
