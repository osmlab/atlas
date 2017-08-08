package org.openstreetmap.atlas.streaming.readers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.AbstractResource;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.scalars.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the {@link CsvReader}
 *
 * @author matthieun
 */
public class CsvReaderTest
{
    private static final Logger logger = LoggerFactory.getLogger(CsvReaderTest.class);

    private static final StringConverter<String> CONVERTER1 = StringConverter.IDENTITY;
    private static final StringConverter<Long> CONVERTER2 = string -> Long.valueOf(string);
    private static final StringConverter<Double> CONVERTER3 = string -> Double.valueOf(string);

    private AbstractResource resource;
    private AbstractResource wrongResource;
    private CsvSchema schema;

    @Before
    public void init()
    {
        this.resource = new InputStreamResource(
                CsvReaderTest.class.getResourceAsStream("data.csv"));
        this.wrongResource = new InputStreamResource(
                CsvReaderTest.class.getResourceAsStream("wrongData.csv"));
        // Create the schema with 3 converters in order.
        this.schema = new CsvSchema(CONVERTER1, CONVERTER2, CONVERTER3);
    }

    @Test
    public void testReader()
    {
        final CsvReader reader = new CsvReader(this.schema, this.resource);
        final Counter counter = new Counter();
        reader.forEachRemaining(csvLine ->
        {
            logger.info(csvLine.toString());
            if (counter.getValue() == 0)
            {
                Assert.assertEquals("Hello,world", csvLine.get(0));
            }
            if (counter.getValue() == 1)
            {
                Assert.assertEquals(36.3, csvLine.get(2));
                Assert.assertEquals(true, csvLine.get(1) instanceof Long);
            }
            counter.increment();
        });
        try
        {
            new CsvReader(this.schema, this.wrongResource).next();
            // Malformed lines are just ignored here.
            // Assert.fail("The wrong resource has to fail.");
        }
        catch (final Exception e)
        {
            logger.info("Wrong resource: " + e.getMessage());
        }
    }
}
