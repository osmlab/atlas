package org.openstreetmap.atlas.geography.atlas.items.complex.boundaries;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class ComplexBoundariesIntegrationTest
{
    @Rule
    public final ComplexBoundaryIntegrationTestRule rule = new ComplexBoundaryIntegrationTestRule();

    @Test
    public void testComplexBoundary()
    {
        final Atlas atlas = this.rule.getAtlas();
        final List<ComplexBoundary> badEntities = new ArrayList<>();
        final ComplexBoundaryFinder finder = new ComplexBoundaryFinder();
        final List<ComplexBoundary> result = Iterables.stream(finder.find(atlas, badEntities::add))
                .collectToList();
        Assert.assertEquals(710, result.size());

        final List<ComplexBoundary> withCountry = Iterables.stream(result)
                .filter(boundary -> Iterables.size(boundary.getCountries()) > 0).collectToList();
        Assert.assertEquals(2, withCountry.size());

        Assert.assertEquals(139, badEntities.size());
    }
}
