package org.openstreetmap.atlas.geography.atlas.pbf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import crosby.binary.osmosis.OsmosisReader;

/**
 * {@link Closeable} version of an {@link OsmosisReader} that prevents {@link InputStream} leaks.
 *
 * @author matthieun
 */
public class CloseableOsmosisReader extends OsmosisReader implements Closeable
{
    private final InputStream input;

    public CloseableOsmosisReader(final InputStream input)
    {
        super(input);
        this.input = input;
    }

    @Override
    public void close() throws IOException
    {
        this.input.close();
    }
}
