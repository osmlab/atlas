package org.openstreetmap.atlas.utilities.time;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class LocalTimeTest
{
    private static final Logger logger = LoggerFactory.getLogger(LocalTimeTest.class);

    @Test
    public void formatTest()
    {
        final Time time = new Time(Duration.milliseconds(1443561351635L));
        logger.info(time.format(DateTimeFormatter.BASIC_ISO_DATE));
        logger.info(time.format(DateTimeFormatter.ISO_DATE_TIME));
        logger.info(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logger.info(time.format("yyyy-MM-dd--HH-mm-ss-SSS"));

        final LocalTime localTime = LocalTime.now(ZoneId.of("America/Los_Angeles"));
        logger.info(localTime.format(DateTimeFormatter.BASIC_ISO_DATE));
        logger.info(localTime.format(DateTimeFormatter.ISO_DATE_TIME));
        logger.info(localTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        // This still fails...
        // logger.info(time.format("yyyy-MM-dd--HH-mm-ss-SSS-zzz"));
    }
}
