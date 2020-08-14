package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystem;

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
                .toLineDelimitedFeatureChanges(true);

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
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final AtlasDiffCommand command = new AtlasDiffCommand();
        command.setNewFileSystem(this.memoryFileSystem);
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand(ATLAS_BEFORE2, ATLAS_AFTER2, "--ldgeojson");
        Assert.assertEquals("atlasB.atlas\n" + this.atlas1to2Diff + "\n", outContent.toString());
        Assert.assertEquals(
                "atlas-diff: warn: Files only in Before Atlas folder:\n"
                        + "atlas-diff: warn: atlasA.atlas\n"
                        + "atlas-diff: warn: Files only in After Atlas folder:\n"
                        + "atlas-diff: warn: atlasC.atlas\n" + "atlas-diff: warn: atlasD.atlas\n",
                errContent.toString());
    }

    @Test
    public void testFolderDiffSameFileList()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final AtlasDiffCommand command = new AtlasDiffCommand();
        command.setNewFileSystem(this.memoryFileSystem);
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand(ATLAS_BEFORE, ATLAS_AFTER, "--ldgeojson", "--recursive");
        Assert.assertEquals("atlasA.atlas\n" + this.atlas1to2Diff + "\n" + "atlasB.atlas\n"
                + AtlasDiffCommand.NO_CHANGE + "\n" + "x/atlasC.atlas\n"
                + AtlasDiffCommand.NO_CHANGE + "\n" + "x/atlasD.atlas\n"
                + AtlasDiffCommand.NO_CHANGE + "\n", outContent.toString());
        Assert.assertEquals("", errContent.toString());
    }

    @Test
    public void testSimpleDiff()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final AtlasDiffCommand command = new AtlasDiffCommand();
        command.setNewFileSystem(this.memoryFileSystem);
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand(ATLAS_A, ATLAS_B, "--ldgeojson");
        Assert.assertEquals(this.atlas1to2Diff + "\n", outContent.toString());
        Assert.assertEquals("", errContent.toString());
    }
}
