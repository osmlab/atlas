package org.openstreetmap.atlas.geography.atlas.items.complex.boundaries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class ComplexBoundaryTest
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexBoundaryTest.class);

    @Rule
    public final ComplexBoundaryTestRule rule = new ComplexBoundaryTestRule();

    @Test
    public void testComplexBoundary()
    {
        final Atlas atlas = this.rule.getAtlas();
        final List<ComplexBoundary> badEntities = new ArrayList<>();
        logger.info("Atlas: {}", atlas);
        final ComplexBoundaryFinder finder = new ComplexBoundaryFinder();
        finder.setWithSubAreas(true);
        final List<ComplexBoundary> result = Iterables.stream(finder.find(atlas, badEntities::add))
                .collectToList();
        logger.info("Complex Boundaries: {}", result);
        Assert.assertEquals(2, result.size());
        final ComplexBoundary first = result.get(0);
        final ComplexBoundary second = result.get(1);
        Assert.assertEquals(2, first.getAdministrativeLevel());
        final Set<ComplexBoundary> firstChildren = first.getSubAreas();
        Assert.assertEquals(1, firstChildren.size());
        final ComplexBoundary firstChild = firstChildren.iterator().next();
        Assert.assertEquals(3, firstChild.getAdministrativeLevel());
        final Set<ComplexBoundary> firstChildChildren = firstChild.getSubAreas();
        Assert.assertEquals(1, firstChildChildren.size());
        final ComplexBoundary firstChildChild = firstChildChildren.iterator().next();
        Assert.assertEquals(4, firstChildChild.getAdministrativeLevel());

        Assert.assertEquals(3, second.getAdministrativeLevel());

        Assert.assertEquals(1, badEntities.size());
        logger.info("Bad Entity: {}", badEntities.get(0));
        logger.info("Bad Entity Reason: {}", badEntities.get(0).getError());
    }
}
