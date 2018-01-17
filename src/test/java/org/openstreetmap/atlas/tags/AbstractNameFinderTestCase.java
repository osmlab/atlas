package org.openstreetmap.atlas.tags;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.openstreetmap.atlas.tags.names.NameFinder;

/**
 * Base class that provides the freezeDry method for verifying serialization works or not.
 *
 * @author cstaylor
 */
abstract class AbstractNameFinderTestCase
{
    protected NameFinder freezeDry(final NameFinder finder) throws Exception
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(finder);
        oos.close();
        final ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray()));
        return (NameFinder) ois.readObject();
    }
}
