package org.openstreetmap.atlas.geography.boundary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.measure.Longitude;
import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Tests for {@link DynamicGridIndexBuilder}.
 *
 * @author mkalender
 */
public class DynamicGridIndexBuilderTest
{
    protected static final Logger logger = LoggerFactory
            .getLogger(DynamicGridIndexBuilderTest.class);

    private static void testAndValidateGridIndexConsistency(final Set<String> countries,
            final String filename)
    {
        // Generate grid index for the first time
        final CountryBoundaryMap firstMap = CountryBoundaryMap.fromPlainText(
                new InputStreamResource(CountryBoundaryMapTest.class.getResourceAsStream(filename))
                        .withDecompressor(Decompressor.GZIP));
        firstMap.initializeGridIndex(countries);

        // Generate grid index for the second time
        final CountryBoundaryMap secondMap = CountryBoundaryMap.fromPlainText(
                new InputStreamResource(CountryBoundaryMapTest.class.getResourceAsStream(filename))
                        .withDecompressor(Decompressor.GZIP));
        secondMap.initializeGridIndex(countries);

        // Compare
        Assert.assertTrue(CountryBoundaryMapCompareCommand.areSTRtreesEqual(firstMap.getGridIndex(),
                secondMap.getGridIndex()));
    }

    @Test
    public void testConsistencyOne()
    {
        final Set<String> countries = new HashSet<>();
        countries.add("HTI");
        countries.add("DOM");

        testAndValidateGridIndexConsistency(countries, "HTI_DOM_osm_boundaries.txt.gz");
    }

    @Test
    public void testConsistencyTwo()
    {
        final Set<String> countries = new HashSet<>();
        countries.add("AIA");
        countries.add("MAF");

        testAndValidateGridIndexConsistency(countries, "MAF_AIA_osm_boundaries.txt.gz");
    }

    @Test
    public void testIndexBuildWithMultipleThreads()
    {
        final Set<String> countries = new HashSet<>();
        countries.add("AIA");
        countries.add("MAF");

        // Generate grid index for the first time
        final CountryBoundaryMap firstMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryBoundaryMapTest.class
                        .getResourceAsStream("MAF_AIA_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final Envelope maxEnvelope = new Envelope(Longitude.MIN_VALUE, Longitude.MAX_VALUE,
                Latitude.MINIMUM.asDegrees(), Latitude.MAXIMUM.asDegrees());
        @SuppressWarnings("unchecked")
        final List<Polygon> boundaries = firstMap.getRawIndex().query(maxEnvelope);
        final DynamicGridIndexBuilder builder = new DynamicGridIndexBuilder(boundaries, maxEnvelope,
                firstMap.getRawIndex());

        // Create several threads and try to initialize index simultaneously
        final int threadPoolSize = 4;
        final STRtree[] indices = new STRtree[threadPoolSize];
        try (Pool testPool = new Pool(threadPoolSize, "grid-index-test"))
        {
            for (int index = 0; index < threadPoolSize; index++)
            {
                final int threadIndex = index;
                testPool.queue(() ->
                {
                    logger.info("Trying to initialize grid index.");
                    indices[threadIndex] = builder.getIndex();
                });
            }
        }
        catch (final Exception e)
        {
            Assert.fail("Grid index creation in multiple threads failed.");
        }

        // Validate
        final STRtree referenceTree = indices[0];
        Assert.assertNotNull(referenceTree);
        for (int index = 1; index < threadPoolSize; index++)
        {
            // Make sure we get the same object - grid index initialization should be done only once
            Assert.assertTrue(referenceTree == indices[index]);
            Assert.assertTrue(CountryBoundaryMapCompareCommand.areSTRtreesEqual(referenceTree,
                    indices[index]));
        }
    }
}
