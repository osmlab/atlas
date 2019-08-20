package org.openstreetmap.atlas.utilities.runtime;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor with single lines to extract some result value
 *
 * @author matthieun
 */
public abstract class SingleLineMonitor extends RunScriptMonitor
{
    private static final Logger logger = LoggerFactory.getLogger(SingleLineMonitor.class);

    private Optional<String> result = Optional.empty();

    public Optional<String> getResult()
    {
        return this.result;
    }

    protected abstract Optional<String> parseResult(String line);

    @Override
    protected void parseStandardError(final Iterable<String> lines)
    {
        parseStandardStream(lines);
    }

    @Override
    protected void parseStandardOutput(final Iterable<String> lines)
    {
        parseStandardStream(lines);
    }

    private void parseStandardStream(final Iterable<String> lines)
    {
        for (final String line : lines)
        {
            // Use the double-if check around the "synchronized" trick, to avoid synchronizing
            // on every line.
            if (!this.result.isPresent())
            {
                final Optional<String> resultOption = parseResult(line);
                if (resultOption.isPresent())
                {
                    synchronized (SingleLineMonitor.class)
                    {
                        if (!this.result.isPresent())
                        {
                            this.result = resultOption;
                            logger.trace("Found result: {}", resultOption.get());
                        }
                    }
                }
            }
        }
    }
}
