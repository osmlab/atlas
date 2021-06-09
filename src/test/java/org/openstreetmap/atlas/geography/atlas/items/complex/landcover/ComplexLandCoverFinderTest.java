package org.openstreetmap.atlas.geography.atlas.items.complex.landcover;

import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;

/**
 * @author samg
 */
public class ComplexLandCoverFinderTest 
{
    @Rule
    public ComplexLandCoverFinderTestRule rule = new ComplexLandCoverFinderTestRule();

    @Test
    public void testComplexLandCoverWithCustomFilter()
    {
        final Atlas atlas = this.rule.getComplexLandCoverWithRelationsAndAreas();
        final ComplexLandCoverFinder landCoverRelationFinder = new ComplexLandCoverFinder();
        final Iterable<ComplexLandCover> complexLandCovers = landCoverRelationFinder.find(atlas,
                TaggableFilter.forDefinition("landuse->VINEYARD|surface->paved"));
        Assert.assertEquals(2, StreamSupport.stream(complexLandCovers.spliterator(), false).count());
    }

    @Test
    public void testLandCoverArea()
    {
        final Atlas atlas = this.rule.getLandCoverAreaAtlas();
        final ComplexLandCoverFinder landCoverRelationFinder = new ComplexLandCoverFinder();
        final Iterable<ComplexLandCover> complexLandCoverAreas = landCoverRelationFinder.find(atlas);
        Assert.assertEquals(2, StreamSupport.stream(complexLandCoverAreas.spliterator(), false).count());
    }

    @Test
    public void testMultipolygonLandCoverRelation()
    {
        final Atlas atlas = this.rule.getMultipolygonLandCoverRelationAtlas();
        final ComplexLandCoverFinder landCoverRelationFinder = new ComplexLandCoverFinder();
        final Iterable<ComplexLandCover> complexLandCoverRelations = landCoverRelationFinder.find(atlas);
        Assert.assertEquals(2,
                StreamSupport.stream(complexLandCoverRelations.spliterator(), false).count());
    }

    @Test
    public void testNonMultipolygonLandCoverRelation()
    {
        final Atlas atlas = this.rule.getNonMultipolygonLandCoverRelationAtlas();
        final ComplexLandCoverFinder landCoverRelationFinder = new ComplexLandCoverFinder();
        final Iterable<ComplexLandCover> complexLandCoverRelations = landCoverRelationFinder.find(atlas);
        Assert.assertFalse(complexLandCoverRelations.iterator().hasNext());
        Assert.assertEquals(0,
                StreamSupport.stream(complexLandCoverRelations.spliterator(), false).count());
    }
}
