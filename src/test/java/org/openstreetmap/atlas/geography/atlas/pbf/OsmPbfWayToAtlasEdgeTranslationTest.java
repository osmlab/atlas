package org.openstreetmap.atlas.geography.atlas.pbf;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;

import com.google.common.collect.Iterables;

/**
 * Tests various OSM Way tagging combinations to test whether Atlas edges will be correctly created.
 *
 * @author mgostintsev
 */
public class OsmPbfWayToAtlasEdgeTranslationTest
{
    @Rule
    public final OsmPbfWayToAtlasEdgeTranslationTestRule rule = new OsmPbfWayToAtlasEdgeTranslationTestRule();

    @Test
    public void testLoadingFerryWithMotorVehicleNoTag()
    {
        final Atlas atlas = this.rule.getFerryRelation5831018Atlas();
        Assert.assertEquals("Three bi-directional way creates 6 edges", 6,
                Iterables.size(atlas.edges()));
        Assert.assertEquals("A single valid relation made up of 3 ways", 1,
                Iterables.size(atlas.relations()));
    }

    @Test
    public void testLoadingPartialRelationWithComplexTagging()
    {
        final Atlas atlas = this.rule.getPartialRelation4451979Atlas();
        Assert.assertEquals(
                "One bi-directional way creates 2 edges, plus an additional one-way edge", 3,
                Iterables.size(atlas.edges()));
        Assert.assertEquals("A single valid relation made up of 3 ways", 1,
                Iterables.size(atlas.relations()));
    }
}
