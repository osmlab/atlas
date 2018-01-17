package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;

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

    @Test
    public void testHarbourFromArea()
    {
        final Atlas harborAsArea = this.rule.getHarborAsAreaAtlas();
        final Iterable<ComplexWaterEntity> waterEntities = new ComplexWaterEntityFinder()
                .find(harborAsArea, Finder::ignore);
        Assert.assertEquals("A single harbor must be created for the Harbor Area in the Atlas.",
                harborAsArea.numberOfAreas(), Iterables.size(waterEntities));
        Assert.assertTrue(waterEntities.iterator().next().getWaterType().equals(WaterType.HARBOUR));
    }

    @Test
    public void testHarbourFromRelation()
    {
        final Atlas harborAsRelation = this.rule.getHarborAsRelationAtlas();
        final Iterable<ComplexWaterEntity> waterEntities = new ComplexWaterEntityFinder()
                .find(harborAsRelation, Finder::ignore);
        Assert.assertEquals("A single harbor must be created for the Harbor Relation in the Atlas.",
                harborAsRelation.numberOfRelations(), Iterables.size(waterEntities));
        Assert.assertTrue(waterEntities.iterator().next().getWaterType().equals(WaterType.HARBOUR));
    }
}
