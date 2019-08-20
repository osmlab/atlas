package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener.TagChangeListener;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Area} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public class CompleteArea extends Area implements CompleteEntity<CompleteArea>
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;
    private long identifier;
    private Polygon polygon;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    private final TagChangeDelegate tagChangeDelegate = TagChangeDelegate.newTagChangeDelegate();

    /**
     * Create a {@link CompleteArea} from a given {@link Area} reference. The {@link CompleteArea}'s
     * fields will match the fields of the reference. The returned {@link CompleteArea} will be
     * full, i.e. all of its associated fields will be non-null.
     *
     * @param area
     *            the {@link Area} to copy
     * @return the full {@link CompleteArea}
     */
    public static CompleteArea from(final Area area)
    {
        if (area instanceof CompleteArea && !((CompleteArea) area).isFull())
        {
            throw new CoreException("Area parameter was a CompleteArea but it was not full: {}",
                    area);
        }
        return new CompleteArea(area.getIdentifier(), area.asPolygon(), area.getTags(),
                area.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    /**
     * Create a shallow {@link CompleteArea} from a given {@link Area} reference. The
     * {@link CompleteArea}'s identifier will match the identifier of the reference {@link Area}.
     * The returned {@link CompleteArea} will be shallow, i.e. all of its associated fields will be
     * null except for the identifier.
     *
     * @param area
     *            the {@link Area} to copy
     * @return the shallow {@link CompleteArea}
     */
    public static CompleteArea shallowFrom(final Area area)
    {
        if (area.bounds() == null)
        {
            throw new CoreException("Area parameter bounds were null");
        }
        return new CompleteArea(area.getIdentifier()).withBoundsExtendedBy(area.bounds());
    }

    public CompleteArea(final Long identifier, final Polygon polygon,
            final Map<String, String> tags, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.bounds = polygon != null ? polygon.bounds() : null;

        this.identifier = identifier;
        this.polygon = polygon;
        this.tags = tags;
        this.relationIdentifiers = relationIdentifiers;
    }

    CompleteArea(final long identifier)
    {
        this(identifier, null, null, null);
    }

    @Override
    public void addTagChangeListener(final TagChangeListener tagChangeListener)
    {
        this.tagChangeDelegate.addTagChangeListener(tagChangeListener);
    }

    @Override
    public Polygon asPolygon()
    {
        return this.polygon;
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    @Override
    public CompleteItemType completeItemType()
    {
        return CompleteItemType.AREA;
    }

    public CompleteArea copy()
    {
        return new CompleteArea(this.identifier, this.polygon, this.tags, this.relationIdentifiers);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteArea)
        {
            final CompleteArea that = (CompleteArea) other;
            return CompleteEntity.basicEqual(this, that)
                    && Objects.equals(this.asPolygon(), that.asPolygon());
        }
        return false;
    }

    @Override
    public void fireTagChangeEvent(final TagChangeEvent tagChangeEvent)
    {
        this.tagChangeDelegate.fireTagChangeEvent(tagChangeEvent);
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean isFull()
    {
        return this.bounds != null && this.polygon != null && this.tags != null
                && this.relationIdentifiers != null;
    }

    @Override
    public boolean isShallow()
    {
        return this.polygon == null && this.tags == null && this.relationIdentifiers == null;
    }

    @Override
    public String prettify(final PrettifyStringFormat format)
    {
        String separator = "";
        if (format == PrettifyStringFormat.MINIMAL_SINGLE_LINE)
        {
            separator = "";
        }
        else if (format == PrettifyStringFormat.MINIMAL_MULTI_LINE)
        {
            separator = "\n";
        }
        final StringBuilder builder = new StringBuilder();

        builder.append(this.getClass().getSimpleName() + " ");
        builder.append("[");
        builder.append(separator);
        builder.append("identifier: " + this.identifier + ", ");
        builder.append(separator);
        if (this.polygon != null)
        {
            builder.append("polygon: " + this.polygon + ", ");
            builder.append(separator);
        }
        if (this.tags != null)
        {
            builder.append("tags: " + this.tags + ", ");
            builder.append(separator);
        }
        if (this.relationIdentifiers != null)
        {
            builder.append("parentRelations: " + this.relationIdentifiers + ", ");
            builder.append(separator);
        }
        builder.append("]");

        return builder.toString();
    }

    @Override
    public Set<Relation> relations()
    {
        /*
         * Note that the Relations returned by this method will technically break the Located
         * contract, since they have null bounds.
         */
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(CompleteRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public void removeTagChangeListeners()
    {
        this.tagChangeDelegate.removeTagChangeListeners();
    }

    @Override
    public void setTags(final Map<String, String> tags)
    {
        this.tags = tags != null ? new HashMap<>(tags) : null;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier + ", polygon="
                + this.polygon + ", tags=" + this.tags + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public String toWkt()
    {
        if (this.polygon == null)
        {
            return null;
        }
        return this.polygon.toWkt();
    }

    public CompleteArea withBoundsExtendedBy(final Rectangle bounds)
    {
        if (this.bounds == null)
        {
            this.bounds = bounds;
            return this;
        }
        this.bounds = Rectangle.forLocated(this.bounds, bounds);
        return this;
    }

    @Override
    public CompleteEntity withGeometry(final Iterable<Location> locations)
    {
        return this.withPolygon(new Polygon(locations));
    }

    @Override
    public CompleteArea withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public CompleteArea withPolygon(final Polygon polygon)
    {
        this.polygon = polygon;
        this.bounds = polygon.bounds();
        return this;
    }

    @Override
    public CompleteArea withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteArea withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }
}
