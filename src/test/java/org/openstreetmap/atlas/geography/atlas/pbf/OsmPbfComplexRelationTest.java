package org.openstreetmap.atlas.geography.atlas.pbf;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;

import com.google.common.collect.Iterables;

/**
 * Tests various OSM Relations
 *
 * @author mgostintsev
 */
public class OsmPbfComplexRelationTest
{
    @Rule
    public final OsmPbfComplexRelationTestRule rule = new OsmPbfComplexRelationTestRule();

    @Test
    public void testLoadingPartialRelation4451979()
    {
        final Atlas atlas = this.rule.getPartialRelation4451979Atlas();
        Assert.assertEquals(1, Iterables.size(atlas.relations()));
    }
}
