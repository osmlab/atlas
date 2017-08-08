package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StreamIterable;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test case rule for {@link SelfIntersectingHighwayAreaTestCase}
 *
 * @author isabellehillberg
 */
public class SelfIntersectingHighwayAreaTestCaseRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "intersection.txt.gz")
    private Atlas intersectionAtlas;

    public StreamIterable<ComplexHighwayArea> findSelfIntersecting()
    {
        return Iterables
                .stream(new ComplexHighwayAreaFinder().find(this.intersectionAtlas, Finder::ignore))
                .filter(ComplexHighwayArea::isSelfIntersecting);
    }
}
