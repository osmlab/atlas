package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StreamIterable;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test case rule for {@link ZeroSizeHighwayAreaTestCase}
 *
 * @author isabellehillberg
 */
public class ZeroSizeHighwayAreaTestCaseRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "size_zero.txt.gz")
    private Atlas zeroSizeAtlas;

    public StreamIterable<ComplexHighwayArea> findZeroSized()
    {
        return Iterables
                .stream(new ComplexHighwayAreaFinder().find(this.zeroSizeAtlas, Finder::ignore))
                .filter(ComplexHighwayArea::isZeroSized);
    }
}
