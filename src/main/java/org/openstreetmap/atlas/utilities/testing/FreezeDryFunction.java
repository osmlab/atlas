package org.openstreetmap.atlas.utilities.testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Function that does a serialize and deserialize pair of operations in memory so we can test if an
 * object will serialize correctly.
 *
 * @author brian_l_davis
 * @author cstaylor
 * @param <T>
 *            the type of object being tested
 */
public class FreezeDryFunction<T extends Serializable> implements Function<T, T>
{
    @Override
    public T apply(final T source) throws CoreException
    {
        try
        {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    byteArrayOutputStream);
            objectOutputStream.writeObject(source);
            objectOutputStream.close();
            final ObjectInputStream objectInputStream = new ObjectInputStream(
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            @SuppressWarnings("unchecked")
            final T result = (T) objectInputStream.readObject();
            return result;
        }
        catch (final Exception oops)
        {
            throw new CoreException("Failure during serialization/deserialization pair", oops);
        }
    }
}
