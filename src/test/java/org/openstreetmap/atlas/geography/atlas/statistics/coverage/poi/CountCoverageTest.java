package org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.statistics.Counter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class CountCoverageTest
{
    private static final Logger logger = LoggerFactory.getLogger(CountCoverageTest.class);

    @Rule
    public final CountCoverageTestCaseRule rule = new CountCoverageTestCaseRule();

    @Test
    public void testPoiCounts()
    {
        testCount("address_housenumber;", 3);
        testCount("amenity;", 3);
        testCount("fixme;", 3);
        testCount("hospitals;", 1);
        testCount("lakes;", 3);
        testCount("rail_stops;", 2);
        testCount("airports;", 1);
    }

    private void testCount(final String type, final int count)
    {
        final Atlas atlas = this.rule.getAtlas();
        final List<SimpleCoverage<AtlasEntity>> coverages = new ArrayList<>();
        SimpleCoverage.parseSimpleCoverages(atlas, Iterables.filter(
                Counter.POI_COUNTS_DEFINITION.getDefault().lines(), line -> line.startsWith(type)))
                .forEach(coverages::add);
        final SimpleCoverage<AtlasEntity> result = coverages.get(0);
        result.run();
        logger.info("{}", result.getStatistic());
        Assert.assertEquals(count, result.getStatistic().values().iterator().next().getCount(),
                0.01);
    }
}
