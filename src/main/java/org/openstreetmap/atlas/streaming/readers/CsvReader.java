package org.openstreetmap.atlas.streaming.readers;

import java.util.Iterator;

import org.openstreetmap.atlas.streaming.resource.AbstractResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reader for a Csv {@link Resource}. This is using openCsv
 *
 * @author tony
 * @author matthieun
 */
public class CsvReader implements Iterator<CsvLine>
{
    public static final Logger logger = LoggerFactory.getLogger(CsvReader.class);

    private final String comment;
    private final Iterator<String> lineIterator;
    private final CsvSchema schema;

    public CsvReader(final CsvSchema schema, final AbstractResource resource)
    {
        this.lineIterator = resource.lines().iterator();
        this.schema = schema;
        this.comment = "#";
    }

    /**
     * @param resource
     *            The resource to read
     * @param schema
     *            The Csv schema to use
     * @param comment
     *            The lines starting with this will be ignored.
     */
    public CsvReader(final CsvSchema schema, final AbstractResource resource, final String comment)
    {
        this.lineIterator = resource.lines().iterator();
        this.schema = schema;
        this.comment = comment;
    }

    @Override
    public boolean hasNext()
    {
        return this.lineIterator.hasNext();
    }

    @Override
    public CsvLine next()
    {
        CsvLine result = null;
        String candidate;

        do
        {
            candidate = this.lineIterator.next();
            if (candidate == null)
            {
                return null;
            }
            result = candidate.startsWith(this.comment) ? null : parse(candidate);
        }
        while (this.lineIterator.hasNext() && result == null);

        return result;
    }

    private CsvLine parse(final String candidate)
    {
        try
        {
            return CsvLine.build(this.schema, candidate);
        }
        catch (final Exception e)
        {
            logger.warn(
                    "Ignoring malformed line: -- " + candidate + " --. Reason: " + e.getMessage());
            return null;
        }
    }
}
