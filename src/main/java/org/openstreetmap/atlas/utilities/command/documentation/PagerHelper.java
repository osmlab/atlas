package org.openstreetmap.atlas.utilities.command.documentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class PagerHelper
{
    private static final String PAGER_ENVIRONMENT_VARIABLE = "PAGER";
    private static final String DEFAULT_PAGER = "less";
    private static final String DEFAULT_PAGER_FLAGS = "-cSRMis";

    public PagerHelper()
    {

    }

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
            System.err.println("TODO: respect for PAGER env var currently unimplemented!");
        }

        final Optional<String> pagerProgram = callWhichOnPager(DEFAULT_PAGER);

        Process process;
        try
        {
            final String[] arguments = { pagerProgram.get(), DEFAULT_PAGER_FLAGS };
            process = Runtime.getRuntime().exec(arguments);
        }
        catch (final Exception exception)
        {
            System.out.println(string);
        }

        // TODO figure out how to pipe to less
        System.out.println(string);
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
