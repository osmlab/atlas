package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.FileSystem;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.diff.AtlasDiff;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.StringList;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author matthieun
 */
public class AtlasDiffCommandTest
{
    private static final String ATLAS_A = "/test/atlasA.atlas";
    private static final String ATLAS_B = "/test/atlasB.atlas";

    private static final String ATLAS_BEFORE = "/test/before";
    private static final String ATLAS_BEFORE_A = ATLAS_BEFORE + "/atlasA.atlas";
    private static final String ATLAS_BEFORE_B = ATLAS_BEFORE + "/atlasB.atlas";
    private static final String ATLAS_BEFORE_C = ATLAS_BEFORE + "/x/atlasC.atlas";
    private static final String ATLAS_BEFORE_D = ATLAS_BEFORE + "/x/atlasD.atlas";

    private static final String ATLAS_BEFORE2 = "/test/before2";
    private static final String ATLAS_BEFORE2_A = ATLAS_BEFORE2 + "/atlasA.atlas";
    private static final String ATLAS_BEFORE2_B = ATLAS_BEFORE2 + "/atlasB.atlas";

    private static final String ATLAS_AFTER = "/test/after";
    private static final String ATLAS_AFTER_A = ATLAS_AFTER + "/atlasA.atlas";
    private static final String ATLAS_AFTER_B = ATLAS_AFTER + "/atlasB.atlas";
    private static final String ATLAS_AFTER_C = ATLAS_AFTER + "/x/atlasC.atlas";
    private static final String ATLAS_AFTER_D = ATLAS_AFTER + "/x/atlasD.atlas";

    private static final String ATLAS_AFTER2 = "/test/after2";
    private static final String ATLAS_AFTER2_B = ATLAS_AFTER2 + "/atlasB.atlas";
    private static final String ATLAS_AFTER2_C = ATLAS_AFTER2 + "/atlasC.atlas";
    private static final String ATLAS_AFTER2_D = ATLAS_AFTER2 + "/atlasD.atlas";

    @Rule
    public AtlasDiffCommandTestRule rule = new AtlasDiffCommandTestRule();

    private FileSystem memoryFileSystem;

    private Atlas atlas1;
    private Atlas atlas2;
    private Atlas atlas3;
    private Atlas atlas4;

    private String atlas1to2Diff;

    @After
    public void cleanup()
    {
        Streams.close(this.memoryFileSystem);
    }

    @Before
    public void setup()
    {
        this.atlas1 = this.rule.getAtlas1();
        this.atlas2 = this.rule.getAtlas2();
        this.atlas3 = this.rule.getAtlas3();
        this.atlas4 = this.rule.getAtlas4();

        this.atlas1to2Diff = new AtlasDiff(this.atlas1, this.atlas2).saveAllGeometries(false)
                .generateChange().orElseThrow(() -> new CoreException("There is no change"))
                .toLineDelimitedFeatureChanges();

        this.memoryFileSystem = Jimfs.newFileSystem(Configuration.unix());

        this.atlas1.save(new File(ATLAS_A, this.memoryFileSystem));
        this.atlas2.save(new File(ATLAS_B, this.memoryFileSystem));

        this.atlas1.save(new File(ATLAS_BEFORE_A, this.memoryFileSystem));
        this.atlas2.save(new File(ATLAS_BEFORE_B, this.memoryFileSystem));
        this.atlas3.save(new File(ATLAS_BEFORE_C, this.memoryFileSystem));
        this.atlas4.save(new File(ATLAS_BEFORE_D, this.memoryFileSystem));

        this.atlas2.save(new File(ATLAS_BEFORE2_A, this.memoryFileSystem));
        this.atlas1.save(new File(ATLAS_BEFORE2_B, this.memoryFileSystem));

        this.atlas2.save(new File(ATLAS_AFTER_A, this.memoryFileSystem));
        this.atlas2.save(new File(ATLAS_AFTER_B, this.memoryFileSystem));
        this.atlas3.save(new File(ATLAS_AFTER_C, this.memoryFileSystem));
        this.atlas4.save(new File(ATLAS_AFTER_D, this.memoryFileSystem));

        this.atlas2.save(new File(ATLAS_AFTER2_B, this.memoryFileSystem));
        this.atlas3.save(new File(ATLAS_AFTER2_C, this.memoryFileSystem));
        this.atlas4.save(new File(ATLAS_AFTER2_D, this.memoryFileSystem));
    }

    @Test
    public void testFolderDiffDifferentFileList()
    {
        final AtlasDiffCommand command = new AtlasDiffCommand()
                .withUnitTestMode(this.memoryFileSystem);
        command.runSubcommand(ATLAS_BEFORE2, ATLAS_AFTER2, "--ldgeojson");
        final List<String> warnings = command.getWarnings();
        Assert.assertEquals(5, warnings.size());
        Assert.assertEquals("atlasA.atlas", warnings.get(1));
        Assert.assertEquals("atlasC.atlas", warnings.get(3));
        Assert.assertEquals("atlasD.atlas", warnings.get(4));
        final List<String> stdout = command.getStdout();
        Assert.assertEquals(2, stdout.size());
        Assert.assertEquals("atlasB.atlas", stdout.get(0));
        Assert.assertEquals(this.atlas1to2Diff, stdout.get(1));
    }

    @Test
    public void testFolderDiffSameFileList()
    {
        final AtlasDiffCommand command = new AtlasDiffCommand()
                .withUnitTestMode(this.memoryFileSystem);
        command.runSubcommand(ATLAS_BEFORE, ATLAS_AFTER, "--ldgeojson", "--recursive");
        Assert.assertTrue(command.getWarnings().isEmpty());
        final List<String> stdout = command.getStdout();
        Assert.assertEquals(8, stdout.size());
        Assert.assertEquals("atlasA.atlas", stdout.get(0));
        Assert.assertEquals(this.atlas1to2Diff, stdout.get(1));
        Assert.assertEquals("x/atlasC.atlas", stdout.get(4));
        Assert.assertEquals(AtlasDiffCommand.NO_CHANGE, stdout.get(5));
    }

    @Test
    public void testSimpleDiff()
    {
        final AtlasDiffCommand command = new AtlasDiffCommand()
                .withUnitTestMode(this.memoryFileSystem);
        command.runSubcommand(ATLAS_A, ATLAS_B, "--ldgeojson");
        Assert.assertTrue(command.getWarnings().isEmpty());
        final List<String> stdout = command.getStdout();
        Assert.assertEquals(1, stdout.size());
        Assert.assertEquals(3, StringList.split(stdout.get(0), System.lineSeparator()).size());
        Assert.assertEquals(this.atlas1to2Diff, stdout.get(0));
    }
}
