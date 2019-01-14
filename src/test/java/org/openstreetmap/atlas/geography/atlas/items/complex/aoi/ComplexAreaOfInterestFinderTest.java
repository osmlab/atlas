package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;

/**
 * Test cases for {@link ComplexAreaOfInterestFinder}
 *
 * @author sayas01
 */
public class ComplexAreaOfInterestFinderTest
{
    @Rule
    public ComplexAreaOfInterestFinderTestRule rule = new ComplexAreaOfInterestFinderTestRule();

    @Test
    public void testMultipolygonAOIRelation()
    {
        final Atlas atlas = this.rule.getMultipolygonAOIRelationAtlas();
        final ComplexAreaOfInterestFinder aoiRelationFinder = new ComplexAreaOfInterestFinder();
        final Iterable<ComplexAreaOfInterest> complexAOIRelations = aoiRelationFinder.find(atlas);
        Assert.assertEquals(2,
                StreamSupport.stream(complexAOIRelations.spliterator(), false).count());
    }

    @Test
    public void testNonMultipolygonAOIRelation()
    {
        final Atlas atlas = this.rule.getNonMultipolygonAOIRelationAtlas();
        final ComplexAreaOfInterestFinder aoiRelationFinder = new ComplexAreaOfInterestFinder();
        final Iterable<ComplexAreaOfInterest> complexAOIRelations = aoiRelationFinder.find(atlas);
        Assert.assertFalse(complexAOIRelations.iterator().hasNext());
        Assert.assertEquals(0,
                StreamSupport.stream(complexAOIRelations.spliterator(), false).count());
    }

    @Test
    public void testAOIArea()
    {
        final Atlas atlas = this.rule.getAoiAreaAtlas();
        final ComplexAreaOfInterestFinder aoiRelationFinder = new ComplexAreaOfInterestFinder();
        final Iterable<ComplexAreaOfInterest> complexAOIAreas = aoiRelationFinder.find(atlas);
        Assert.assertEquals(2, StreamSupport.stream(complexAOIAreas.spliterator(), false).count());
    }

    @Test
    public void testComplexAOIWithCustomFilter()
    {
        final Atlas atlas = this.rule.getComplexAOIWithRelationsAndAreas();
        final ComplexAreaOfInterestFinder aoiRelationFinder = new ComplexAreaOfInterestFinder();
        final Iterable<ComplexAreaOfInterest> complexAOIs = aoiRelationFinder.find(atlas,
                TaggableFilter.forDefinition("landuse->VINEYARD|amenity->SCHOOL"));
        Assert.assertEquals(3, StreamSupport.stream(complexAOIs.spliterator(), false).count());
    }
}
