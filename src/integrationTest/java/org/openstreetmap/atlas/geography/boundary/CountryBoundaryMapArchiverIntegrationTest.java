package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.TemporaryFile;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * Test CountryBoundaryMap generation.
 *
 * @author james-gage
 * @author matthieun
 */
@Ignore
public class CountryBoundaryMapArchiverIntegrationTest
{
    @Test
    public void testOceanBoundary() throws IOException
    {
        try (TemporaryFile temporary = File.temporary("CountryBoundaryMapArchiverTest", ".txt.gz"))
        {
            final StringList arguments = new StringList();
            arguments.add("-" + CountryBoundaryMapArchiver.BOUNDARY_FILE.getName() + "="
                    + CountryBoundaryMapArchiverIntegrationTest.class
                            .getResource("oceanTestBoundary.txt").getPath());

            arguments.add("-" + CountryBoundaryMapArchiver.OUTPUT.getName() + "="
                    + temporary.getAbsolutePathString());
            arguments.add(
                    "-" + CountryBoundaryMapArchiver.OCEAN_BOUNDARY_ZOOM_LEVEL.getName() + "=3");
            arguments
                    .add("-" + CountryBoundaryMapArchiver.CREATE_SPATIAL_INDEX.getName() + "=true");
            new CountryBoundaryMapArchiver().runWithoutQuitting(arguments.toArray());
            // ensure that the correct number of ocean boundaries are generated
            final CountryBoundaryMap oceanBoundaryMap = CountryBoundaryMap.fromPlainText(temporary);
            oceanBoundaryMap.writeToFile(new File("/Users/samuelgass/Desktop/test.txt"));
            Assert.assertEquals(57, oceanBoundaryMap.size());
            final CountryBoundaryMap test = CountryBoundaryMap
                    .fromPlainText(new File("/Users/samuelgass/Desktop/test.txt"));
            Assert.assertEquals(57, test.size());
        }
    }
}
