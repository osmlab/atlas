package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;

import com.google.common.collect.Iterables;

/**
 * Test creating water ways from different types of {@link Relation}s. Some {@link Relation}s are
 * comprised of individual water entities and we want to make sure we build each individual piece
 * without building the the relation itself, to avoid duplicate water entities. Other
 * {@link Relation}s are comprised of non-water entities, in which case we want to make sure we
 * build only a single water entity for the parent {@link Relation}, without doing anything to the
 * members.
 *
 * @author mgostintsev
 */
public class ComplexWaterWayTest
{
    @Rule
    public final ComplexWaterWayTestRule rule = new ComplexWaterWayTestRule();

    @Test
    public void testWaterWayAsRelationOfWaterWays()
    {
        final Atlas canalAsRelationOfCanalEntities = this.rule
                .getCanalAsRelationOfCanalEntitiesAtlas();
        final Iterable<ComplexWaterEntity> waterEntities = new ComplexWaterEntityFinder()
                .find(canalAsRelationOfCanalEntities, Finder::ignore);

        Assert.assertEquals(
                "The number of water entities should be equal to the number of relation members",
                canalAsRelationOfCanalEntities.relation(6006326000000L).members().size(),
                Iterables.size(waterEntities));
    }

    @Test
    public void testWaterWaysAsRelationOfNonWaterWays()
    {
        final Atlas canalAsRelationOfNonCanalEntities = this.rule
                .getCanalAsRelatonOfNonCanalEntitiesAtlas();
        final Iterable<ComplexWaterEntity> waterEntities = new ComplexWaterEntityFinder()
                .find(canalAsRelationOfNonCanalEntities, Finder::ignore);

        Assert.assertEquals(
                "The number of water entities should be equal to the number of relations in the Atlas",
                Iterables.size(canalAsRelationOfNonCanalEntities.relations()),
                Iterables.size(waterEntities));
    }
}
