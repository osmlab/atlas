package org.openstreetmap.atlas.utilities.command.documentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;

/**
 * @author lcram
 */
public class PagerHelper
{
    private static final String PAGER_ENVIRONMENT_VARIABLE = "ATLAS_SHELL_TOOLS_PAGER";
    private static final String PAGER_FALLBACK_VARIABLE = "PAGER";
    private static final String DEFAULT_PAGER = "less -cSRMis";

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
        final String pagerFallbackVariable = System.getenv(PAGER_FALLBACK_VARIABLE);
        final Optional<String> pagerProgram;
        final String[] pagerFlags;

        if (pagerVariable != null && !pagerVariable.isEmpty())
        {
            pagerProgram = callWhichOnPager(pagerVariable.split("\\s+")[0]);
            pagerFlags = extractFlagsFromVariable(pagerVariable);
        }
        else if (pagerFallbackVariable != null && !pagerFallbackVariable.isEmpty())
        {
            pagerProgram = callWhichOnPager(pagerFallbackVariable.split("\\s+")[0]);
            pagerFlags = extractFlagsFromVariable(pagerFallbackVariable);
        }
        else
        {
            pagerProgram = callWhichOnPager(DEFAULT_PAGER.split("\\s+")[0]);
            pagerFlags = extractFlagsFromVariable(DEFAULT_PAGER);
        }

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
            final String[] processBuilderArguments = new String[1 + pagerFlags.length + 1];
            processBuilderArguments[0] = pagerProgram.orElseThrow(AtlasShellToolsException::new);
            if (pagerFlags.length > 0)
            {
                System.arraycopy(pagerFlags, 0, processBuilderArguments, 1, pagerFlags.length);
            }
            processBuilderArguments[processBuilderArguments.length - 1] = temporaryFile
                    .getAbsolutePath();
            final ProcessBuilder processBuilder = new ProcessBuilder(processBuilderArguments);
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
        return Optional.of(line);
    }

    private String[] extractFlagsFromVariable(final String variable)
    {
        final String[] flags = variable.split("\\s+");
        return Arrays.copyOfRange(flags, 1, flags.length);
    }
}
