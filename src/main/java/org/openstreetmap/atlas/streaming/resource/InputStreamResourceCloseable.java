package org.openstreetmap.atlas.streaming.resource;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Create an InputStream resource that depends upon other resources that should not be closed until
 * the caller is done with the InputStream
 * 
 * @author Taylor Smock
 */
public class InputStreamResourceCloseable extends InputStreamResource implements ResourceCloseable
{
    private final AutoCloseable[] dependencies;

    /**
     * Create a new InputStreamWritableResource
     * 
     * @param inputStreamSupplier
     *            The InputStream to write to
     * @param dependencies
     *            The dependencies that should be closed on finish
     */
    @SafeVarargs
    public InputStreamResourceCloseable(final Supplier<InputStream> inputStreamSupplier,
            final AutoCloseable... dependencies)
    {
        super(inputStreamSupplier);
        this.dependencies = dependencies != null
                ? Stream.of(dependencies).filter(Objects::nonNull).toArray(AutoCloseable[]::new)
                : new AutoCloseable[0];
    }

    @Override
    public AutoCloseable[] getDependencies()
    {
        return this.dependencies;
    }
}
