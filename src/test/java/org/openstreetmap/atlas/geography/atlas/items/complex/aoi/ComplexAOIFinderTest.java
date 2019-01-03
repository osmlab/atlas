package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;

/**
 * Test cases for {@link ComplexAOIFinder}
 *
 * @author sayas01
 */
public class ComplexAOIFinderTest
{
    @Rule
    public ComplexAOIFinderTestRule rule = new ComplexAOIFinderTestRule();

    @Test
    public void testMultipolygonAOIRelation()
    {
        final Atlas atlas = this.rule.getMultipolygonAOIRelationAtlas();
        final ComplexAOIFinder aoiRelationFinder = new ComplexAOIFinder();
        final Iterable<ComplexAOI> complexAOIRelations = aoiRelationFinder.find(atlas);
        Assert.assertEquals(StreamSupport.stream(complexAOIRelations.spliterator(), false).count(),
                2);
    }

    @Test
    public void testNonMultipolygonAOIRelation()
    {
        final Atlas atlas = this.rule.getNonMultipolygonAOIRelationAtlas();
        final ComplexAOIFinder aoiRelationFinder = new ComplexAOIFinder();
        final Iterable<ComplexAOI> complexAOIRelations = aoiRelationFinder.find(atlas);
        Assert.assertFalse(complexAOIRelations.iterator().hasNext());
        Assert.assertEquals(StreamSupport.stream(complexAOIRelations.spliterator(), false).count(),
                0);
    }

    @Test
    public void testAOIArea()
    {
        final Atlas atlas = this.rule.getAoiAreaAtlas();
        final ComplexAOIFinder aoiRelationFinder = new ComplexAOIFinder();
        final Iterable<ComplexAOI> complexAOIAreas = aoiRelationFinder.find(atlas);
        Assert.assertEquals(StreamSupport.stream(complexAOIAreas.spliterator(), false).count(), 2);
    }

    @Test
    public void testComplexAOIWithCustomFilter()
    {
        final Atlas atlas = this.rule.getComplexAOIWithRelationsAndAreas();
        final ComplexAOIFinder aoiRelationFinder = new ComplexAOIFinder();
        final Iterable<ComplexAOI> complexAOIs = aoiRelationFinder.find(atlas,
                TaggableFilter.forDefinition("landuse->VINEYARD|amenity->SCHOOL"));
        Assert.assertEquals(StreamSupport.stream(complexAOIs.spliterator(), false).count(), 3);
    }
}
