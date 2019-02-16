package org.openstreetmap.atlas.geography.atlas.changeset;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deserializes {@link ChangeSet} objects from {@link OutputStream}s back into {@link ChangeSet}
 * objects.
 *
 * @author mkalender
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public class BinaryChangeSetDeserializer implements ChangeSetDeserializer
{
    private static final Logger logger = LoggerFactory.getLogger(BinaryChangeSetDeserializer.class);

    private final ObjectInputStream resource;
    private boolean hasMore;

    public BinaryChangeSetDeserializer(final Resource resourceToReadFrom) throws IOException
    {
        this.resource = new ObjectInputStream(resourceToReadFrom.read());
        this.hasMore = true;
    }

    @Override
    public void close() throws Exception
    {
        this.resource.close();
    }

    @Override
    public Optional<ChangeSet> get()
    {
        if (!this.hasMore)
        {
            return Optional.empty();
        }

        ChangeSet changeSet = null;

        try
        {
            changeSet = (ChangeSet) this.resource.readObject();
        }
        catch (final EOFException e)
        {
            this.hasMore = false;

            try
            {
                this.close();
            }
            catch (final Exception closeException)
            {
                logger.error("ChangeSet resource close is failed.", closeException);
            }
        }
        catch (final ClassNotFoundException | IOException e)
        {
            logger.error("ChangeSet deserialization is failed.", e);
        }

        return Optional.ofNullable(changeSet);
    }
}
