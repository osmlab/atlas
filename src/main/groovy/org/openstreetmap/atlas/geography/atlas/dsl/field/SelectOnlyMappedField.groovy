package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Function

/**
 * Similar to a SelectOnlyField but the value is derived from a mapping function.
 *
 * @author Yazad Khambata
 */
class SelectOnlyMappedField<T, R> extends SelectOnlyField {
    private Function<T, R> mapper

    SelectOnlyMappedField(final String name, Function<T, R> mapper) {
        super(name)
        this.mapper = mapper
    }

    @Override
    def read(final AtlasEntity atlasEntity) {
        final Object originalValue = super.read(atlasEntity)

        def mappedValue = mapper.apply(originalValue)

        mappedValue
    }

    @Override
    boolean equals(final Object o) {
        return super.equals(o)
    }

    @Override
    int hashCode() {
        return super.hashCode()
    }
}
