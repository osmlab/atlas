package org.openstreetmap.atlas.geography.atlas.changeset;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes {@link ChangeSet} objects and writes them into {@link OutputStream}s in binary format.
 *
 * @author mkalender
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public class BinaryChangeSetSerializer implements ChangeSetSerializer
{
    private static final Logger logger = LoggerFactory.getLogger(BinaryChangeSetSerializer.class);

    private final ObjectOutputStream resource;

    public BinaryChangeSetSerializer(final WritableResource resourceToWriteInto) throws IOException
    {
        this.resource = new ObjectOutputStream(resourceToWriteInto.write());
    }

    @Override
    public void accept(final ChangeSet changeSet)
    {
        try
        {
            this.resource.writeObject(changeSet);
            this.resource.flush();
        }
        catch (final IOException e)
        {
            logger.error("ChangeSet serialization is failed.", e);
        }
    }

    @Override
    public void close() throws Exception
    {
        this.resource.close();
    }
}
