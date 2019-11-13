package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * A field that can only be used in the select list of a query.
 *
 * @author Yazad Khambata
 */
class SelectOnlyField extends AbstractField implements Selectable {

    SelectOnlyField(final String name) {
        super(name)
    }

    /**
     * Maintaining of a Mapping of MetaMethod(s) by Class since MetaMethod cached on a class like
     * say MultiNode cannot be applied to a different class PackedNode.
     */
    private Map<Class<? extends AtlasEntity>, MetaMethod> metaMethods = [:]

    private boolean optionalReturnType

    @Override
    def read(final AtlasEntity atlasEntity) {
        final Class<? extends AtlasEntity> clazz = atlasEntity.class

        MetaMethod metaMethod = null

        try {
            // Groovy Meta-classes do not include methods that have default implementations in interfaces,
            // hence special handling is needed.
            if (name == "osmTags") {
                return atlasEntity.getOsmTags()
            }

            //Detect Access Style.
            final String fieldToLookFor = getName()

            metaMethod = metaMethods[clazz]

            if (metaMethod == null) {

                synchronized (this) {

                    if (metaMethod == null) {
                        metaMethod = atlasEntity.metaClass.methods.stream()
                                .filter({ m -> m.name == fieldToLookFor || m.name == "get${fieldToLookFor.substring(0, 1).toUpperCase()}${fieldToLookFor.substring(1)}" })
                                .filter({ m -> m.getParameterTypes().size() == 0 })
                                .findFirst()
                                .get()

                        metaMethods[clazz] = metaMethod
                        optionalReturnType = metaMethod.getReturnType() == Optional.class
                    }
                }
            }

            Valid.notEmpty metaMethod, "metaMethod is NULL for ${getName()}."

            //Access
            final Object value = metaMethod.invoke(atlasEntity, null)

            //Optional handling
            if (!optionalReturnType) {
                return value
            }

            return ((Optional) value).orElse(null)
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't access ${getName()} (metaMethod identified: ${metaMethod}; optionalReturnType identified: ${optionalReturnType}) from ${atlasEntity}", e)
        }
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
