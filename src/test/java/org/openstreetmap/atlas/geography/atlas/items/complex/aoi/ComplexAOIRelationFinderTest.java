package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Test cases for {@link ComplexAOIRelationFinder}
 *
 * @author sayas01
 */
public class ComplexAOIRelationFinderTest
{
    @Rule
    public ComplexAOIRelationFinderTestRule rule = new ComplexAOIRelationFinderTestRule();

    @Test
    public void testMultipolygonAOIRelation()
    {
        final Atlas atlas = this.rule.getMultipolygonAOIRelationAtlas();
        final ComplexAOIRelationFinder aoiRelationFinder = new ComplexAOIRelationFinder();
        final Iterable<ComplexAOIRelation> complexAOIRelations = aoiRelationFinder.find(atlas);
        Assert.assertEquals(StreamSupport.stream(complexAOIRelations.spliterator(), false).count(),
                2);
    }

    @Test
    public void testNonMultipolygonAOIRelation()
    {
        final Atlas atlas = this.rule.getNonMultipolygonAOIRelationAtlas();
        final ComplexAOIRelationFinder aoiRelationFinder = new ComplexAOIRelationFinder();
        final Iterable<ComplexAOIRelation> complexAOIRelations = aoiRelationFinder.find(atlas);
        Assert.assertFalse(complexAOIRelations.iterator().hasNext());
        Assert.assertEquals(StreamSupport.stream(complexAOIRelations.spliterator(), false).count(),
                0);
    }

    @Test
    public void testNonRelationAOIAtlas()
    {
        final Atlas atlas = this.rule.getNonRelationAOIAtlas();
        final ComplexAOIRelationFinder aoiRelationFinder = new ComplexAOIRelationFinder();
        final Iterable<ComplexAOIRelation> complexAOIRelations = aoiRelationFinder.find(atlas);
        Assert.assertFalse(complexAOIRelations.iterator().hasNext());
        Assert.assertEquals(StreamSupport.stream(complexAOIRelations.spliterator(), false).count(),
                0);
    }
}
