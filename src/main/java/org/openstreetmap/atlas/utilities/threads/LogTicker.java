package org.openstreetmap.atlas.utilities.threads;

import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class LogTicker extends Ticker
{
    private static final Logger logger = LoggerFactory.getLogger(LogTicker.class);

    public LogTicker(final String name, final Duration tickerTime)
    {
        super(name, tickerTime);
    }

    @Override
    protected void tickAction(final Duration sinceStart)
    {
        logger.info("{}: {}", getName(), sinceStart);
    }
}
