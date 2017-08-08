package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StreamIterable;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test data for {@link ComplexHighwayAreaTestCase}
 *
 * @author isabellehillberg
 */
public class ComplexHighwayAreaTestCaseRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "validHighwayArea_1.txt.gz")
    private Atlas validAtlas1;

    @TestAtlas(loadFromTextResource = "validHighwayArea.txt.gz")
    private Atlas validAtlas;

    @TestAtlas(loadFromTextResource = "validHighwayArea_2.txt.gz")
    private Atlas validAtlas2;

    @TestAtlas(loadFromTextResource = "noHighwayArea.txt.gz")
    private Atlas noHighwayAreaAtlas;

    @TestAtlas(loadFromTextResource = "invalidHighwayArea.txt.gz")
    private Atlas invalidHighwayAreaAtlas;

    public StreamIterable<ComplexHighwayArea> invalidHighwayArea()
    {
        return Iterables.stream(
                new ComplexHighwayAreaFinder().find(this.invalidHighwayAreaAtlas, Finder::ignore));
    }

    public StreamIterable<ComplexHighwayArea> noHighwayArea()
    {
        return Iterables.stream(
                new ComplexHighwayAreaFinder().find(this.noHighwayAreaAtlas, Finder::ignore));
    }

    public StreamIterable<ComplexHighwayArea> validHighwayArea()
    {
        return Iterables
                .stream(new ComplexHighwayAreaFinder().find(this.validAtlas, Finder::ignore));
    }

    public StreamIterable<ComplexHighwayArea> validHighwayArea1()
    {
        return Iterables
                .stream(new ComplexHighwayAreaFinder().find(this.validAtlas1, Finder::ignore));
    }

    public StreamIterable<ComplexHighwayArea> validHighwayArea2()
    {
        return Iterables
                .stream(new ComplexHighwayAreaFinder().find(this.validAtlas2, Finder::ignore));
    }
}
