package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Complex entity built on the fly from an existing {@link Atlas}. Examples include buildings with
 * holes (Relation type=multipolygon with inner and outer members, and a building=yes tag), lakes
 * with islands, etc.
 *
 * @author matthieun
 */
public abstract class ComplexEntity implements AtlasObject
{
    /**
     * Validation errors are reported through this class to any interested callers
     *
     * @author cstaylor
     */
    public static final class ComplexEntityError implements Serializable
    {
        private static final long serialVersionUID = 3162352792545207168L;

        private final transient ComplexEntity source;
        private final String reason;
        private final Throwable oops;

        public ComplexEntityError(final ComplexEntity source, final String reason)
        {
            this(source, reason, null);
        }

        public ComplexEntityError(final ComplexEntity source, final String reason,
                final Throwable oops)
        {
            this.source = source;
            this.reason = reason;
            this.oops = oops;
        }

        public Throwable getException()
        {
            return this.oops;
        }

        public String getReason()
        {
            return this.reason;
        }

        public ComplexEntity getSource()
        {
            return this.source;
        }

        @Override
        public String toString()
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream stream = new PrintStream(baos);
            stream.printf("%s\n", this.source);
            stream.printf("OSM id: %d\n", this.source.getOsmIdentifier());
            if (this.reason != null)
            {
                stream.printf("Why: %s\n", this.reason);
            }
            if (this.oops != null)
            {
                this.oops.printStackTrace(stream);
            }
            stream.close();
            return new String(baos.toByteArray(), Charset.forName("UTF-8"));
        }

    }

    private static final long serialVersionUID = -553026286746440299L;

    private final Atlas atlas;

    private final AtlasEntity source;

    private Tuple<String, Throwable> invalidReason;

    protected ComplexEntity(final AtlasEntity source)
    {
        this.source = source;
        this.atlas = source.getAtlas();
    }

    @Override
    public Rectangle bounds()
    {
        return getSource().bounds();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof ComplexEntity)
        {
            return other.getClass().equals(this.getClass())
                    && this.getSource().equals(((ComplexEntity) other).getSource());
        }
        return false;
    }

    public List<ComplexEntityError> getAllInvalidations()
    {
        final List<ComplexEntityError> returnValue = new ArrayList<>();
        if (!isValid())
        {
            getError().ifPresent(returnValue::add);
        }
        return returnValue;
    }

    @Override
    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Optional<ComplexEntityError> getError()
    {
        if (this.invalidReason == null)
        {
            return Optional.empty();
        }
        return Optional.of(new ComplexEntityError(this, this.invalidReason.getFirst(),
                this.invalidReason.getSecond()));
    }

    @Override
    public long getIdentifier()
    {
        return getSource().getIdentifier();
    }

    @Override
    public long getOsmIdentifier()
    {
        return this.source.getOsmIdentifier();
    }

    public AtlasEntity getSource()
    {
        return this.source;
    }

    @Override
    public Optional<String> getTag(final String tagName)
    {
        return getSource().getTag(tagName);
    }

    @Override
    public Map<String, String> getTags()
    {
        return getSource().getTags();
    }

    @Override
    public int hashCode()
    {
        return this.source.hashCode();
    }

    /**
     * @return True if there are not any missing data in the Atlas to be able to properly build this
     *         {@link ComplexEntity}. This is used so any Atlas does not offer any
     *         {@link ComplexEntity} that would be compromised to an end user.
     */
    public boolean isValid()
    {
        return this.invalidReason == null;
    }

    @Override
    public abstract String toString();

    protected void setInvalidReason(final String reason, final Throwable oops)
    {
        this.invalidReason = new Tuple<>(reason, oops);
    }
}
