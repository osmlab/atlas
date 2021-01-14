package org.openstreetmap.atlas.streaming.resource;

import java.io.OutputStream;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Create an OutputStream resource that depends upon other resources that should not be closed until
 * the caller is done with the OutputStream
 * 
 * @author Taylor Smock
 */
public class OutputStreamWritableResourceCloseable extends OutputStreamWritableResource
        implements WritableResourceCloseable
{
    private final AutoCloseable[] dependencies;

    /**
     * Create a new OutputStreamWritableResource
     * 
     * @param out
     *            The OutputStream to write to
     * @param dependencies
     *            The dependencies that should be closed on finish
     */
    public OutputStreamWritableResourceCloseable(final OutputStream out,
            final AutoCloseable... dependencies)
    {
        super(out);
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
