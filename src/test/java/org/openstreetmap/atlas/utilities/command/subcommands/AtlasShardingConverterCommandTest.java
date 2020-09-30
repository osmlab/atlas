package org.openstreetmap.atlas.utilities.command.subcommands;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.testing.OsmFileParser;
import org.openstreetmap.atlas.utilities.testing.OsmFileToPbf;

/**
 * @author matthieun
 */
public class AtlasShardingConverterCommandTest
{
    @Test
    public void testConvert()
    {
        final File folder = File.temporaryFolder();
        try
        {
            createTestAtlas(folder);
            final File output = folder.child("output");
            final StringList arguments = new StringList();
            arguments.add("--input=" + folder.child("input").getAbsolutePathString());
            arguments.add("--inputSharding=slippy@11");
            arguments.add("--output=" + output.getAbsolutePathString());
            arguments.add("--outputSharding=geohash@4");

            final String[] args = new String[arguments.size()];
            for (int index = 0; index < arguments.size(); index++)
            {
                args[index] = arguments.get(index);
            }
            final AtlasShardingConverterCommand command = new AtlasShardingConverterCommand();
            command.runSubcommand(args);

            Assert.assertEquals(2, output.listFilesRecursively().size());
            Assert.assertEquals(5,
                    new AtlasResourceLoader().load(output.child("FRA_gbsf.atlas")).numberOfEdges());
            Assert.assertEquals(8,
                    new AtlasResourceLoader().load(output.child("FRA_gbsg.atlas")).numberOfEdges());
        }
        finally
        {
            folder.deleteRecursively();
        }
    }

    private void createTestAtlas(final File folder)
    {
        final Resource resource = new InputStreamResource(() -> PbfToAtlasCommandTest.class
                .getResourceAsStream("shardingConverter.josm.osm"));
        final File pbfFile = folder.child("shardingConverter.pbf");
        final StringResource osmFile = new StringResource();
        new OsmFileParser().update(resource, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);

        final StringList arguments = new StringList();
        arguments.add(pbfFile.getAbsolutePathString());
        arguments.add("--output=" + folder.getAbsolutePathString());
        arguments.add("--countryName=FRA");

        final String[] args = new String[arguments.size()];
        for (int index = 0; index < arguments.size(); index++)
        {
            args[index] = arguments.get(index);
        }
        new PbfToAtlasCommand().runSubcommand(args);

        final Atlas full = new AtlasResourceLoader()
                .load(folder.child("FRA_shardingConverter.atlas"));
        final File input = folder.child("input");
        input.mkdirs();
        final SlippyTile tile1 = SlippyTile.forName("11-998-708");
        full.subAtlas(tile1.bounds(), AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("Should be there."))
                .save(input.child("FRA_" + tile1.getName() + FileSuffix.ATLAS));
        final SlippyTile tile2 = SlippyTile.forName("11-998-709");
        full.subAtlas(tile2.bounds(), AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("Should be there."))
                .save(input.child("FRA_" + tile2.getName() + FileSuffix.ATLAS));
    }
}
