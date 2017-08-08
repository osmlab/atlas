package org.openstreetmap.atlas.streaming.resource;

import java.io.InputStream;
import java.util.function.Supplier;

import org.openstreetmap.atlas.streaming.compression.Decompressor;

/**
 * Readable resource from an {@link InputStream}. This is readable once only when using the
 * {@link InputStream} constructor, and readable as many times as needed when using the
 * {@link Supplier} constructor.
 *
 * @author matthieun
 */
public class InputStreamResource extends AbstractResource
{
    private final Supplier<InputStream> inputStreamSupplier;

    public InputStreamResource(final InputStream input)
    {
        this(() -> input);
    }

    public InputStreamResource(final Supplier<InputStream> inputStreamSupplier)
    {
        this.inputStreamSupplier = inputStreamSupplier;
    }

    public InputStreamResource withDecompressor(final Decompressor decompressor)
    {
        this.setDecompressor(decompressor);
        return this;
    }

    public InputStreamResource withName(final String name)
    {
        this.setName(name);
        return this;
    }

    @Override
    protected InputStream onRead()
    {
        return this.inputStreamSupplier.get();
    }
}
