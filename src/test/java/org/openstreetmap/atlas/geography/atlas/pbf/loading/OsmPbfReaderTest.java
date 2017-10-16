package org.openstreetmap.atlas.geography.atlas.pbf.loading;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.streaming.resource.File;

import com.google.common.collect.Iterables;

/**
 * Tests {@link org.openstreetmap.atlas.geography.atlas.pbf.loading.RawAtlasGenerator} Raw Atlas
 * creation and does basic parity check on feature counts between the old and new PBF ingest
 * methods.
 *
 * @author mgostintsev
 */
public class OsmPbfReaderTest
{
    @Test
    public void testParityBetweenRawAtlasAndGeneratedAtlas()
    {
        // Previous PBF-to-Atlas Implementation
        final String pbfPath = OsmPbfReaderTest.class.getResource("9-433-268.osm.pbf").getPath();
        final OsmPbfLoader loader = new OsmPbfLoader(new File(pbfPath), AtlasLoadingOption
                .createOptionWithNoSlicing().setLoadWaysSpanningCountryBoundaries(false));
        final Atlas oldAtlas = loader.read();

        // Raw Atlas Implementation
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(pbfPath));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        Assert.assertEquals(
                "The original Atlas counts of (Lines + Master Edges + Areas) should equal the total number of all Lines in the Raw Atlas, let's verify this",
                Iterables.size(Iterables.filter(oldAtlas.edges(), edge -> edge.isMasterEdge()))
                        + oldAtlas.numberOfAreas() + oldAtlas.numberOfLines(),
                rawAtlas.numberOfLines());

        Assert.assertEquals("The two Atlas files should have identical number of Relations",
                oldAtlas.numberOfRelations(), rawAtlas.numberOfRelations());

        // Note: Nodes/Points in the old PBF-to-Atlas implementation vs. Points in Raw Atlas
        // implementation are difficult to compare, due to us bringing in every Way shape-point.
        // Skipping this check here.
    }

    @Test
    public void testRawAtlasCreation()
    {
        final String path = OsmPbfReaderTest.class.getResource("9-433-268.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas atlas = rawAtlasGenerator.build();

        // The Raw Atlas should never contain Nodes, Edges or Areas
        Assert.assertTrue(atlas.numberOfNodes() == 0);
        Assert.assertTrue(atlas.numberOfEdges() == 0);
        Assert.assertTrue(atlas.numberOfAreas() == 0);

        // Only Points, Lines and Relations
        Assert.assertTrue(atlas.numberOfPoints() == 55265);
        Assert.assertTrue(atlas.numberOfLines() == 6080);
        Assert.assertTrue(atlas.numberOfRelations() == 3);
    }
}
