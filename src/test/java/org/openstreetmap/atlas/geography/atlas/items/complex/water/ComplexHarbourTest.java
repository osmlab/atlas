package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.finder.ComplexWaterEntityFinder;

import com.google.common.collect.Iterables;

/**
 * Tests {@link ComplexWaterEntity} of type {@link WaterType#HARBOUR} creation.
 *
 * @author mgostintsev
 */
public class ComplexHarbourTest
{
    @Rule
    public final ComplexHarborTestRule rule = new ComplexHarborTestRule();

    private final ComplexWaterEntityFinder complexWaterEntityFinder = new ComplexWaterEntityFinder();

    @Test
    public void testHarbourFromArea()
    {
        final Atlas harborAsArea = this.rule.getHarborAsAreaAtlas();
        final Iterable<ComplexWaterEntity> waterEntities = this.complexWaterEntityFinder
                .find(harborAsArea, Finder::ignore);
        Assert.assertEquals("A single harbor must be created for the Harbor Area in the Atlas.",
                harborAsArea.numberOfAreas(), Iterables.size(waterEntities));
        Assert.assertEquals(WaterType.HARBOUR, waterEntities.iterator().next().getWaterType());
    }

    @Test
    public void testHarbourFromRelation()
    {
        final Atlas harborAsRelation = this.rule.getHarborAsRelationAtlas();
        final Iterable<ComplexWaterEntity> waterEntities = this.complexWaterEntityFinder
                .find(harborAsRelation, Finder::ignore);
        Assert.assertEquals("A single harbor must be created for the Harbor Relation in the Atlas.",
                harborAsRelation.numberOfRelations(), Iterables.size(waterEntities));
        Assert.assertEquals(WaterType.HARBOUR, waterEntities.iterator().next().getWaterType());
    }
}
