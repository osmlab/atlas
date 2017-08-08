package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test case rule for the {@link OutOfOrderEdgesHighwayAreaTestCase}
 *
 * @author cstaylor
 */
public class OutOfOrderEdgesHighwayAreaTestCaseRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "out_of_order_highway_area.txt.gz")
    private Atlas outOfOrderAtlas;

    public ComplexHighwayArea invalidHighwayArea()
    {
        return Iterables
                .first(new ComplexHighwayAreaFinder().find(this.outOfOrderAtlas, Finder::ignore))
                .get();
    }
}
