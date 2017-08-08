package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.AreaTestRule;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for AtlasSplitterWithSlippyTileCommand
 *
 * @author yalimu
 */
public class AtlasSplitterWithSlippyTileCommandTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(AtlasSplitterWithSlippyTileCommandTest.class);

    @Rule
    public final AreaTestRule rule = new AreaTestRule();
    private AtlasSplitterWithSlippyTileCommand objectUnderTest;

    @Before
    public void setup()
    {
        this.objectUnderTest = new AtlasSplitterWithSlippyTileCommand();
    }

    @Test
    public void testRun()
    {
        final String testAtlasFile = AtlasSplitterWithSlippyTileCommand.class.getResource("")
                .getPath() + "test" + FileSuffix.ATLAS;
        generateTestAtlasFile(testAtlasFile);
        final CommandMap command = new CommandMap();
        command.put("input", new File(testAtlasFile));
        command.put("zoom_level", 16);
        command.put("output",
                Paths.get(AtlasSplitterWithSlippyTileCommand.class.getResource("").getPath()));
        command.put("combine", false);

        this.objectUnderTest.execute(command);
        combineOutputAndCleanUp();
    }

    private void generateTestAtlasFile(final String filePath)
    {
        final Atlas atlas = this.rule.getAtlas();
        final PackedAtlas saveMe = new PackedAtlasCloner().cloneFrom(atlas);
        saveMe.save(new File(filePath));
    }

    private void combineOutputAndCleanUp()
    {
        final AtlasJoinerSubCommand joinerSubCommand = new AtlasJoinerSubCommand();
        final CommandMap command = new CommandMap();

        logger.info("Checking if splitted Atlas can be combined into the original Atlas");
        final String inputFile = AtlasSplitterWithSlippyTileCommand.class.getResource("").getPath();
        command.put("input", new File(inputFile));
        command.put("output", Paths.get(inputFile + "combined_output" + FileSuffix.ATLAS));
        command.put("combine", false);
        joinerSubCommand.execute(command);

        final Atlas atlasOriginal = new AtlasResourceLoader().load(new File(
                AtlasSplitterWithSlippyTileCommand.class.getResource("test.atlas").getPath()));
        final Atlas atlasCombined = new AtlasResourceLoader()
                .load(new File(inputFile + "combined_output" + FileSuffix.ATLAS));

        Assert.assertNotNull(atlasOriginal);
        Assert.assertNotNull(atlasCombined);

        Assert.assertEquals("Check Edges", atlasCombined.size().getEdgeNumber(),
                atlasOriginal.size().getEdgeNumber());
        Assert.assertEquals("Check Area", atlasCombined.size().getAreaNumber(),
                atlasOriginal.size().getAreaNumber());
        Assert.assertEquals("Check Line", atlasCombined.size().getLineNumber(),
                atlasOriginal.size().getLineNumber());
        Assert.assertEquals("Check Point", atlasCombined.size().getPointNumber(),
                atlasOriginal.size().getPointNumber());

        removeAllAtlasFiles(inputFile);
    }

    private void removeAllAtlasFiles(final String inputFile)
    {
        try
        {
            Files.list(Paths.get(inputFile)).forEach(name ->
            {
                if (name.getFileName().toString().contains(FileSuffix.ATLAS.toString()))
                {
                    new File(name.toAbsolutePath().toString()).delete();
                }
            });
        }
        catch (final IOException e)
        {
            logger.error("Error deleting files" + e.getMessage());
        }
    }
}
