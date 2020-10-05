package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author matthieun
 */
public class AtlasShardingConverterCommandTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final File input = new File("/Users/foo/input", filesystem);
            final File output = new File("/Users/foo/output", filesystem);
            final StringList arguments = new StringList();
            arguments.add("--input=" + input.getAbsolutePathString());
            arguments.add("--inputSharding=slippy@11");
            arguments.add("--output=" + output.getAbsolutePathString());
            arguments.add("--outputSharding=geohash@4");

            final String[] args = new String[arguments.size()];
            for (int index = 0; index < arguments.size(); index++)
            {
                args[index] = arguments.get(index);
            }
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasShardingConverterCommand command = new AtlasShardingConverterCommand();
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));
            command.setNewFileSystem(filesystem);
            command.runSubcommand(args);

            Assert.assertTrue(outContent.toString().isEmpty());
            Assert.assertEquals(
                    "sharding-converter: Found input files: [/Users/foo/input/FRA_11-998-708.atlas, /Users/foo/input/FRA_11-998-709.atlas]\n"
                            + "sharding-converter: Found 2 input shards: [[SlippyTile: zoom = 11, x = 998, y = 709], [SlippyTile: zoom = 11, x = 998, y = 708]]\n"
                            + "sharding-converter: Found country: FRA\n"
                            + "sharding-converter: Found 4 output shards: [[GeoHashTile: value = gbsd], [GeoHashTile: value = gbse], [GeoHashTile: value = gbsf], [GeoHashTile: value = gbsg]]\n"
                            + "sharding-converter: Processing output shard [GeoHashTile: value = gbsd]\n"
                            + "sharding-converter: Loading Atlas with [/Users/foo/input/FRA_11-998-709.atlas]\n"
                            + "sharding-converter: Saving Atlas to /Users/foo/output/FRA_gbsd.atlas\n"
                            + "sharding-converter: Processing output shard [GeoHashTile: value = gbse]\n"
                            + "sharding-converter: Loading Atlas with [/Users/foo/input/FRA_11-998-708.atlas, /Users/foo/input/FRA_11-998-709.atlas]\n"
                            + "sharding-converter: Saving Atlas to /Users/foo/output/FRA_gbse.atlas\n"
                            + "sharding-converter: Processing output shard [GeoHashTile: value = gbsf]\n"
                            + "sharding-converter: Loading Atlas with [/Users/foo/input/FRA_11-998-709.atlas]\n"
                            + "sharding-converter: Saving Atlas to /Users/foo/output/FRA_gbsf.atlas\n"
                            + "sharding-converter: Processing output shard [GeoHashTile: value = gbsg]\n"
                            + "sharding-converter: Loading Atlas with [/Users/foo/input/FRA_11-998-708.atlas, /Users/foo/input/FRA_11-998-709.atlas]\n"
                            + "sharding-converter: Saving Atlas to /Users/foo/output/FRA_gbsg.atlas\n",
                    errContent.toString());
            final File franceAtlas1 = output.child("FRA_gbsf.atlas");
            final File franceAtlas2 = output.child("FRA_gbsg.atlas");
            Assert.assertEquals(2, output.listFilesRecursively().size());
            Assert.assertEquals(5,
                    new AtlasResourceLoader()
                            .load(new InputStreamResource(franceAtlas1::read)
                                    .withName(franceAtlas1.getAbsolutePathString()))
                            .numberOfEdges());
            Assert.assertEquals(8,
                    new AtlasResourceLoader()
                            .load(new InputStreamResource(franceAtlas2::read)
                                    .withName(franceAtlas2.getAbsolutePathString()))
                            .numberOfEdges());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final Resource resource = new InputStreamResource(
                () -> AtlasShardingConverterCommandTest.class
                        .getResourceAsStream("shardingConverter.josm.osm"));
        final File pbfFile = new File("/Users/foo/shardingConverter.pbf", filesystem);
        final StringResource osmFile = new StringResource();
        new OsmFileParser().update(resource, osmFile);
        new OsmFileToPbf().update(osmFile, pbfFile);

        final StringList arguments = new StringList();
        arguments.add(pbfFile.getAbsolutePathString());
        arguments.add("--output=/Users/foo");
        arguments.add("--countryName=FRA");

        final String[] args = new String[arguments.size()];
        for (int index = 0; index < arguments.size(); index++)
        {
            args[index] = arguments.get(index);
        }
        final PbfToAtlasCommand command = new PbfToAtlasCommand();
        command.setNewFileSystem(filesystem);
        command.runSubcommand(args);

        final File outputAtlasFile = new File("/Users/foo/FRA_shardingConverter.atlas", filesystem);
        final Atlas outputAtlas = new AtlasResourceLoader()
                .load(new InputStreamResource(outputAtlasFile::read)
                        .withName(outputAtlasFile.getAbsolutePathString()));
        final File input = new File("/Users/foo/input", filesystem);
        input.mkdirs();
        final SlippyTile tile1 = SlippyTile.forName("11-998-708");
        outputAtlas.subAtlas(tile1.bounds(), AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("Should be there."))
                .saveAsText(input.child("FRA_" + tile1.getName() + FileSuffix.ATLAS));
        final SlippyTile tile2 = SlippyTile.forName("11-998-709");
        outputAtlas.subAtlas(tile2.bounds(), AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("Should be there."))
                .saveAsText(input.child("FRA_" + tile2.getName() + FileSuffix.ATLAS));
    }
}
