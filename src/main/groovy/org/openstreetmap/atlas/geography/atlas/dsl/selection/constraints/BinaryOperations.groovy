package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.openstreetmap.atlas.geography.GeometricSurface
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.polygon.GeometricSurfaceSupport
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.regex.RegexSupport
import org.openstreetmap.atlas.geography.atlas.dsl.query.InnerSelectWrapper
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Operations that can be performed in the where clause.
 *
 * @author Yazad Khambata
 */
enum BinaryOperations implements BinaryOperation {
    eq("eq"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            actualValue == valueToCheck
        }
    },
    lt("lt"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            (actualValue <=> valueToCheck) == -1
        }
    },
    gt("gt"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            (actualValue <=> valueToCheck) == 1
        }
    },
    le("le"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            (actualValue <=> valueToCheck) <= 0
        }
    },
    ge("ge"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            (actualValue <=> valueToCheck) >= 0
        }
    },

    ne("not", "ne"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            !eq.perform(actualValue, valueToCheck, entityClass)
        }
    },

    /**
     * Within bounds
     */
    within("within"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            final AtlasEntity atlasEntity = (AtlasEntity) actualValue

            GeometricSurface geometricSurfaceToCheckIfEntityIsWithin
            if (valueToCheck instanceof List) {
                final List<List<BigDecimal>> geometricSurfaceLocations = (List<List<BigDecimal>>) valueToCheck
                geometricSurfaceToCheckIfEntityIsWithin = GeometricSurfaceSupport.instance.toGeometricSurface(geometricSurfaceLocations)
            } else if (valueToCheck instanceof GeometricSurface) {
                geometricSurfaceToCheckIfEntityIsWithin = (GeometricSurface)valueToCheck
            } else {
                throw new IllegalArgumentException("Unsupported ${valueToCheck?.class} : ${valueToCheck}")
            }

            Valid.notEmpty geometricSurfaceToCheckIfEntityIsWithin

            atlasEntity.within(geometricSurfaceToCheckIfEntityIsWithin)
        }
    },

    /**
     * Typical SQL in clause. Here actualValue is a scalar value like identifier or osmIdentifier.
     */
    inside("in"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            final List<Object> values = (List<Object>) valueToCheck

            actualValue in values
        }
    },

    /**
     * Inner queries
     */
    inner_query("inner_query"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            final InnerSelectWrapper<E> innerSelectWrapper = valueToCheck

            actualValue in innerSelectWrapper.identifiers
        }
    },

    tag("tag", "tagged"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            final Map<String, String> tags = actualValue

            //valueToCheck could be a String (key) or a Map(key, string-value) or a Map(key, list-of-string-values).
            TagOperationHelper.instance.has(tags, valueToCheck)
        }
    },

    tag_like("tag_like", "tagged_like"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            final Map<String, String> tags = actualValue

            TagOperationHelper.instance.like(tags, valueToCheck)
        }
    },

    like("like"){
        @Override
        <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
            final String actualValueAsString = actualValue
            final String regexValueToCheckAsString = valueToCheck

            RegexSupport.instance.matches(actualValueAsString, regexValueToCheckAsString)
        }
    };

    String[] tokens

    BinaryOperations(String[] tokens) {
        this.tokens = tokens
    }

    def <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass) {
        throw new UnsupportedOperationException("The operation ${this} is not supported currently. actualValue: ${actualValue}; valueToCheck: ${valueToCheck} for entity class: ${entityClass}.")
    }

    static BinaryOperation fromToken(final String token) {
        final BinaryOperation operation = Arrays.stream(BinaryOperations.values())
                .filter({ operation -> token in operation.tokens })
                .findFirst()
                .orElseThrow { new IllegalArgumentException("token: ${token}") }

        operation
    }
}
