package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.islands.ComplexIsland;
import org.openstreetmap.atlas.geography.atlas.items.complex.islands.ComplexIslandFinder;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.collect.Iterables;

/**
 * @author Sid
 * @author sbhalekar
 */
public class ComplexIslandTest extends AbstractWaterIslandTest
{
    private Atlas atlas;

    @Before
    public void setUp()
    {
        this.atlas = this.getAtlasBuilder().get();
    }

    @Test
    public void testComplexIslands()
    {
        int islands = 0;
        for (final ComplexIsland island : new ComplexIslandFinder().find(this.atlas,
                Finder::ignore))
        {
            Assert.assertTrue(island.getSource() instanceof Relation);
            islands += island.getGeometry().outers().size();
        }
        Assert.assertEquals("Number of islands should be equal to number of inner polygons", 2,
                islands);
    }

    @Test
    public void testComplexIslandsWithNoData()
    {
        final AtlasBuilder atlasBuilder = new PackedAtlasBuilder();
        // Outer
        atlasBuilder.addArea(0L, Polygon.SILICON_VALLEY, Maps.hashMap());
        // Inner
        atlasBuilder.addArea(1L, Polygon.SILICON_VALLEY_2, Maps.hashMap());
        final RelationBean multipolygon = new RelationBean();
        multipolygon.addItem(0L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.AREA);
        multipolygon.addItem(1L, RelationTypeTag.MULTIPOLYGON_ROLE_INNER, ItemType.AREA);
        atlasBuilder.addRelation(0L, 0L, multipolygon,
                Maps.hashMap(RelationTypeTag.KEY, RelationTypeTag.MULTIPOLYGON_TYPE));

        final Iterable<ComplexIsland> islands = new ComplexIslandFinder().find(atlasBuilder.get(),
                Finder::ignore);
        Assert.assertTrue(Iterables.isEmpty(islands));
    }

}
