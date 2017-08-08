package org.openstreetmap.atlas.geography.boundary;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.CountrySlicingProcessorTest;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author Yiqing Jin
 */
public class GridIndexBuilderTest
{
    private static Logger logger = LoggerFactory.getLogger(GridIndexBuilderTest.class);
    private static CountryBoundaryMap boundaryMap;
    private static Envelope MAX_ENVELOPE = new Envelope(-180, 180, -90, 90);

    @BeforeClass
    public static void setup()
    {
        boundaryMap = new CountryBoundaryMap(new File(
                CountrySlicingProcessorTest.class.getResource("CIV_GIN_LBR.shp").getPath()));
    }

    @Test
    // this test is used for debugging and takes a long time to run so disable in CI
    @Ignore
    public void testDynamicIndexBuilder()
    {
        final STRtree rawIndex = boundaryMap.getRawIndex();
        @SuppressWarnings("unchecked")
        final List<Polygon> polygons = rawIndex.query(MAX_ENVELOPE);

        final DynamicGridIndexBuilder builder = new DynamicGridIndexBuilder(polygons, MAX_ENVELOPE,
                null);

        long start = System.currentTimeMillis();
        final STRtree gridIndex = builder.getIndex();
        long time = System.currentTimeMillis() - start;

        final MultiMap<String, Envelope> cells = builder.getSpatialIndexCells();
        for (final String name : cells.keySet())
        {
            final List<Envelope> boxes = cells.get(name);
            logger.info(name + " : " + boxes.size());
        }

        logger.info("dynamic index size: {}, time cost: {}", gridIndex.size(), time);

        final FixedGridIndexBuilder fBuilder = new FixedGridIndexBuilder(polygons, MAX_ENVELOPE);
        start = System.currentTimeMillis();
        final STRtree fixedIndex = fBuilder.getIndex();
        time = System.currentTimeMillis() - start;
        logger.info("Fixed index size: {}, time cost: {}", fixedIndex.size(), time);

    }
}
