package org.openstreetmap.atlas.streaming.resource;

import java.io.InputStream;
import java.io.OutputStream;

import org.openstreetmap.atlas.streaming.StringInputStream;
import org.openstreetmap.atlas.streaming.StringOutputStream;

/**
 * A {@link Resource} that relies on a {@link String} for convenience.
 *
 * @author matthieun
 */
public class StringResource extends AbstractWritableResource
{
    private String source;
    private StringOutputStream out;

    /**
     * A {@link StringResource} for Writing, then Reading
     */
    public StringResource()
    {
        this.out = new StringOutputStream();
    }

    public StringResource(final AbstractResource source)
    {
        final StringBuilder builder = new StringBuilder();
        source.lines().forEach(line ->
        {
            builder.append(line);
            builder.append("\n");
        });
        this.source = builder.toString();
    }

    public StringResource(final InputStream source)
    {
        this(new InputStreamResource(source));
    }

    public StringResource(final String source)
    {
        this.source = source;
    }

    @Override
    public long length()
    {
        if (this.source != null)
        {
            return this.source.length();
        }
        return super.length();
    }

    public StringResource withName(final String name)
    {
        this.setName(name);
        return this;
    }

    public String writtenString()
    {
        return this.out.toString();
    }

    @Override
    protected InputStream onRead()
    {
        return new StringInputStream(this.source != null ? this.source : writtenString());
    }

    @Override
    protected OutputStream onWrite()
    {
        if (this.out == null)
        {
            this.out = new StringOutputStream();
        }
        return this.out;
    }
}
