package org.openstreetmap.atlas.geography.atlas.dsl.field

import groovy.transform.PackageScope
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
 * Abstraction of a Field in a Table.
 *
 * @author Yazad Khambata
 */
@PackageScope
abstract class AbstractField implements Field {

    private String name

    AbstractField(String name) {
        this.name = name
    }

    @Override
    String getName() {
        name
    }

    @Override
    String getAlias() {
        name
    }

    @Override
    String toString() {
        return "${this.getClass().simpleName}(${name})"
    }

    @Override
    boolean equals(final o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        final AbstractField that = (AbstractField) o

        if (name != that.name) return false

        return true
    }

    @Override
    int hashCode() {
        return (name != null ? name.hashCode() : 0)
    }
}
