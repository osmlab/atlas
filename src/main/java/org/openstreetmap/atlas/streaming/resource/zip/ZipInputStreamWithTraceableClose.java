package org.openstreetmap.atlas.streaming.resource.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ZipInputStreamWithTraceableClose extends ZipInputStream
{
    private static final Logger logger = LoggerFactory
            .getLogger(ZipInputStreamWithTraceableClose.class);
    private final String name;

    public ZipInputStreamWithTraceableClose(final InputStream in, final String name)
    {
        super(in);
        this.name = name;
    }

    @Override
    public void close() throws IOException
    {
        logger.error("Closing ZipInputStream {}", this.name);
        super.close();
    }
}
