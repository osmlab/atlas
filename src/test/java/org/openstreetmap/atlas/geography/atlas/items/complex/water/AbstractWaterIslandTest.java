package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author Sid
 */
public abstract class AbstractWaterIslandTest
{
    private AtlasBuilder atlasBuilder;

    public AbstractWaterIslandTest()
    {
        setUp();
    }

    protected AtlasBuilder getAtlasBuilder()
    {
        return this.atlasBuilder;
    }

    private void setUp()
    {
        this.atlasBuilder = new PackedAtlasBuilder();
        // Outer
        this.atlasBuilder.addArea(0L, Polygon.SILICON_VALLEY, Maps.hashMap());
        // Inner
        this.atlasBuilder.addArea(1L, Polygon.SILICON_VALLEY_2, Maps.hashMap());
        // Multi-polygon for outline
        final RelationBean multipolygon = new RelationBean();
        multipolygon.addItem(0L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.AREA);
        multipolygon.addItem(1L, RelationTypeTag.MULTIPOLYGON_ROLE_INNER, ItemType.AREA);

        // Lake
        this.atlasBuilder.addRelation(0L, 0L, multipolygon,
                Maps.hashMap(RelationTypeTag.KEY, RelationTypeTag.MULTIPOLYGON_TYPE, NaturalTag.KEY,
                        NaturalTag.WATER.name().toLowerCase(), WaterTag.KEY,
                        WaterTag.LAKE.name().toLowerCase()));

        // Sea
        this.atlasBuilder.addRelation(7L, 0L, multipolygon,
                Maps.hashMap(RelationTypeTag.KEY, RelationTypeTag.MULTIPOLYGON_TYPE, NaturalTag.KEY,
                        NaturalTag.WATER.name().toLowerCase(), WaterTag.KEY,
                        WaterTag.SEA.name().toLowerCase()));

        // Reservoir
        this.atlasBuilder.addRelation(1L, 1L, multipolygon,
                Maps.hashMap(RelationTypeTag.KEY, RelationTypeTag.MULTIPOLYGON_TYPE, NaturalTag.KEY,
                        NaturalTag.WATER.name().toLowerCase(), WaterTag.KEY,
                        WaterTag.RESERVOIR.name().toLowerCase()));

        // Rivers
        this.atlasBuilder.addArea(2L, Polygon.SILICON_VALLEY_2,
                Maps.hashMap(NaturalTag.KEY, NaturalTag.WATER.name().toLowerCase(), WaterTag.KEY,
                        WaterTag.RIVER.name().toLowerCase()));
        this.atlasBuilder.addLine(3L, PolyLine.TEST_POLYLINE,
                Maps.hashMap(NaturalTag.KEY, NaturalTag.WATER.name().toLowerCase(), WaterTag.KEY,
                        WaterTag.RIVER.name().toLowerCase()));

        // Drain
        this.atlasBuilder.addLine(4L, PolyLine.TEST_POLYLINE,
                Maps.hashMap(NaturalTag.KEY, NaturalTag.WATER.name().toLowerCase(), WaterwayTag.KEY,
                        WaterwayTag.DRAIN.name().toLowerCase()));
        this.atlasBuilder.addArea(5L, Polygon.SILICON_VALLEY,
                Maps.hashMap(NaturalTag.KEY, NaturalTag.WATER.name().toLowerCase(), WaterwayTag.KEY,
                        WaterwayTag.DRAIN.name().toLowerCase()));

        // Canal
        this.atlasBuilder.addLine(6L, PolyLine.TEST_POLYLINE,
                Maps.hashMap(NaturalTag.KEY, NaturalTag.WATER.name().toLowerCase(), WaterTag.KEY,
                        WaterTag.CANAL.name().toLowerCase()));

        // Pool
        this.atlasBuilder.addArea(7L, Polygon.SILICON_VALLEY,
                Maps.hashMap(NaturalTag.KEY, NaturalTag.WATER.name().toLowerCase(), WaterTag.KEY,
                        WaterTag.POOL.name().toLowerCase()));
    }
}
