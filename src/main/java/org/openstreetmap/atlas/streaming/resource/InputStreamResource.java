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

    /**
     * This constructor is deprecated. Creating an InputStreamResource using a raw InputStream leads
     * to many gotchas, since the resource becomes read-once. For e.g. loading an atlas from an
     * InputStreamResource will fail, since atlas uses an under-the-hood read to determine the
     * serialization format.
     *
     * @param input
     *            the input stream
     */
    @Deprecated
    public InputStreamResource(final InputStream input)
    {
        this(() -> input);
    }

    /**
     * The supplier given to this constructor should return a new input stream at each invocation to
     * avoid any read-once gotchas. E.g. of a good supplier: () -> new FileInputStream("foo.txt")
     *
     * @param inputStreamSupplier
     *            the stream supplier
     */
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
