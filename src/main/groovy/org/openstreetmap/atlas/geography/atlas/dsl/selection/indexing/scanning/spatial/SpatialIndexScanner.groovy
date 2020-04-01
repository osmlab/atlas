package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.spatial

import org.openstreetmap.atlas.geography.GeometricSurface
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.polygon.GeometricSurfaceSupport
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.AbstractIndexScanner
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.ItemType

/**
 * Analogous to a spatial index scanner in a database that supports geo-spatial data.
 *
 * @author Yazad Khambata
 */
@Singleton
class SpatialIndexScanner<E extends AtlasEntity> extends AbstractIndexScanner<E, Object> {

    /**
     * @param atlasTable - The AtlasTable.
     * @param lookupValue - expects [ [ [x, y], [a, b] ] ] (This is the same format as bounds within a GeoJson).
     *                      Alternatively GeometricSurface can be directly passed as well.
     * @return
     */
    @Override
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final Object lookupValue) {
        final Atlas atlas = toAtlas(atlasTable)
        final ItemType itemType = toItemType(atlasTable)

        final SpatialIndexerSetting spatialIndexerSetting = SpatialIndexerSetting.from(itemType)
        final GeometricSurface geometricSurfaceToLookup = toGeometricSurface(lookupValue)
        spatialIndexerSetting.invokeEntitySpecificIndexAwareMethodName(atlas, geometricSurfaceToLookup)
    }

    private GeometricSurface toGeometricSurface(final Object lookupValue) {
        if (lookupValue instanceof GeometricSurface) {
            return lookupValue
        }

        GeometricSurfaceSupport.instance.toGeometricSurface((List<List<List<BigDecimal>>>) lookupValue)
    }

    private enum SpatialIndexerSetting {
        NODE,

        POINT,

        LINE,

        EDGE,

        RELATION("relationsWithEntitiesWithin"),

        AREA;

        private String entitySpecificIndexAwareMethodName

        SpatialIndexerSetting() {
            this(null)
        }

        SpatialIndexerSetting(final String entitySpecificIndexAwareMethodName) {
            this.entitySpecificIndexAwareMethodName = entitySpecificIndexAwareMethodName
        }

        static SpatialIndexerSetting from(final ItemType itemType) {
            itemType.name() as SpatialIndexerSetting
        }

        String getEntitySpecificIndexAwareMethodName() {
            //Example nodesWithin
            entitySpecificIndexAwareMethodName ?: "${this.name().toLowerCase()}sWithin"
        }

        def <E extends AtlasEntity> Iterable<E> invokeEntitySpecificIndexAwareMethodName(final Atlas atlas, final GeometricSurface geometricSurface) {
            atlas."${getEntitySpecificIndexAwareMethodName()}"(geometricSurface)
        }
    }
}
