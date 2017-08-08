package org.openstreetmap.atlas.utilities.runtime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.SplittableInputStream;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.utilities.runtime.RunScriptMonitor.PrinterMonitor;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public final class RunScript
{
    private static final Logger logger = LoggerFactory.getLogger(RunScript.class);

    public static void run(final String script)
    {
        run(script, new ArrayList<>());
    }

    public static void run(final String script, final List<RunScriptMonitor> monitors)
    {
        int returnValue = 0;
        try
        {
            final String[] env = System.getenv().entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toList()).toArray(new String[0]);
            final Process process = Runtime.getRuntime().exec(script, env);
            final PrinterMonitor printer = new PrinterMonitor(logger);
            SplittableInputStream standardOut = null;
            SplittableInputStream standardErr = null;

            if (monitors != null && !monitors.isEmpty())
            {
                standardOut = new SplittableInputStream(process.getInputStream());
                standardErr = new SplittableInputStream(process.getErrorStream());

                final List<InputStream> otherStandardOuts = new ArrayList<>();
                final List<InputStream> otherStandardErrs = new ArrayList<>();
                for (@SuppressWarnings("unused")
                final RunScriptMonitor monitor : monitors)
                {
                    otherStandardOuts.add(standardOut.split());
                    otherStandardErrs.add(standardErr.split());
                }

                // Launch the output monitors
                printer.parse(standardOut, standardErr);
                for (int index = 0; index < monitors.size(); index++)
                {
                    final RunScriptMonitor monitor = monitors.get(index);
                    final InputStream otherStandardOut = otherStandardOuts.get(index);
                    final InputStream otherStandardErr = otherStandardErrs.get(index);
                    monitor.parse(otherStandardOut, otherStandardErr);
                }
            }
            else
            {
                printer.parse(process.getInputStream(), process.getErrorStream());
            }

            returnValue = process.waitFor();

            // Wait for the monitors
            printer.waitForCompletion(Duration.ONE_SECOND);

            if (monitors != null && !monitors.isEmpty())
            {
                for (final RunScriptMonitor monitor : monitors)
                {
                    monitor.waitForCompletion(Duration.ONE_SECOND);
                }

                Streams.close(standardOut);
                Streams.close(standardErr);
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not launch script \"{}\"", script, e);
        }
        if (returnValue != 0)
        {
            throw new CoreException("Non-Zero return value {} when running script \"{}\".",
                    returnValue, script);
        }
    }

    private RunScript()
    {
    }
}
