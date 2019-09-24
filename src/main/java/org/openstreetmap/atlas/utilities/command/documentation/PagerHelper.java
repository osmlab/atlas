package org.openstreetmap.atlas.utilities.command.documentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;

/**
 * @author lcram
 */
public class PagerHelper
{
    private static final String PAGER_ENVIRONMENT_VARIABLE = "ATLAS_SHELL_TOOLS_PAGER";
    private static final String DEFAULT_PAGER = "less";
    private static final String DEFAULT_PAGER_FLAGS = "-cSRMis";

    /**
     * Page a given string using the system paging program. Will check PAGER environment variable
     * first and use that if possible.
     *
     * @param string
     *            the string to page
     */
    public void pageString(final String string)
    {
        final String pagerVariable = System.getenv(PAGER_ENVIRONMENT_VARIABLE);
        if (pagerVariable != null && !pagerVariable.isEmpty())
        {
            System.err.println( // NOSONAR
                    "TODO: respect for ATLAS_SHELL_TOOLS_PAGER env var currently unimplemented!"); // NOSONAR
        }

        final Optional<String> pagerProgram = callWhichOnPager(DEFAULT_PAGER);

        File temporaryFile = null;
        try
        {
            temporaryFile = File.temporary();
        }
        catch (final Exception exception)
        {
            System.out.println(string); // NOSONAR
            return;
        }

        if (temporaryFile == null)
        {
            System.out.println(string); // NOSONAR
            return;
        }

        temporaryFile.writeAndClose(string);

        try
        {
            final ProcessBuilder processBuilder = new ProcessBuilder(
                    pagerProgram.orElseThrow(AtlasShellToolsException::new), DEFAULT_PAGER_FLAGS,
                    temporaryFile.getAbsolutePath());
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            final Process process = processBuilder.start();
            process.waitFor();
        }
        catch (final Exception exception)
        {
            System.out.println(string); // NOSONAR
        }
        finally
        {
            temporaryFile.delete();
        }
    }

    private Optional<String> callWhichOnPager(final String pager)
    {
        final String whichProgram = "which";
        final Process process;
        try
        {
            process = new ProcessBuilder(whichProgram, pager).start();
        }
        catch (final IOException exception)
        {
            return Optional.empty();
        }

        final InputStream stream = process.getInputStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;

        try
        {
            line = reader.readLine();
        }
        catch (final IOException exception)
        {
            return Optional.empty();
        }

        if (line == null || line.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.ofNullable(line);
    }
}
