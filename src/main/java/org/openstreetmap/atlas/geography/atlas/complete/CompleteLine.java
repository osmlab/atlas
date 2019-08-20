package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener.TagChangeListener;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Line} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public class CompleteLine extends Line implements CompleteLineItem<CompleteLine>
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;
    private long identifier;
    private PolyLine polyLine;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    private final TagChangeDelegate tagChangeDelegate = TagChangeDelegate.newTagChangeDelegate();

    /**
     * Create a {@link CompleteLine} from a given {@link Line} reference. The {@link CompleteLine}'s
     * fields will match the fields of the reference. The returned {@link CompleteLine} will be
     * full, i.e. all of its associated fields will be non-null.
     *
     * @param line
     *            the {@link Line} to copy
     * @return the full {@link CompleteLine}
     */
    public static CompleteLine from(final Line line)
    {
        if (line instanceof CompleteLine && !((CompleteLine) line).isFull())
        {
            throw new CoreException("Line parameter was a CompleteLine but it was not full: {}",
                    line);
        }
        return new CompleteLine(line.getIdentifier(), line.asPolyLine(), line.getTags(),
                line.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    /**
     * Create a shallow {@link CompleteLine} from a given {@link Line} reference. The
     * {@link CompleteLine}'s identifier will match the identifier of the reference {@link Line}.
     * The returned {@link CompleteLine} will be shallow, i.e. all of its associated fields will be
     * null except for the identifier.
     *
     * @param line
     *            the {@link Line} to copy
     * @return the shallow {@link CompleteLine}
     */
    public static CompleteLine shallowFrom(final Line line)
    {
        if (line.bounds() == null)
        {
            throw new CoreException("Line parameter bounds were null");
        }
        return new CompleteLine(line.getIdentifier()).withBoundsExtendedBy(line.bounds());
    }

    public CompleteLine(final Long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.bounds = polyLine != null ? polyLine.bounds() : null;

        this.identifier = identifier;
        this.polyLine = polyLine;
        this.tags = tags;
        this.relationIdentifiers = relationIdentifiers;
    }

    CompleteLine(final long identifier)
    {
        this(identifier, null, null, null);
    }

    @Override
    public void addTagChangeListener(final TagChangeListener tagChangeListener)
    {
        this.tagChangeDelegate.addTagChangeListener(tagChangeListener);
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.polyLine;
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    @Override
    public CompleteItemType completeItemType()
    {
        return CompleteItemType.LINE;
    }

    public CompleteLine copy()
    {
        return new CompleteLine(this.identifier, this.polyLine, this.tags,
                this.relationIdentifiers);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteLine)
        {
            final CompleteLine that = (CompleteLine) other;
            return CompleteEntity.basicEqual(this, that)
                    && Objects.equals(this.asPolyLine(), that.asPolyLine());
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
        return this.bounds != null && this.polyLine != null && this.tags != null
                && this.relationIdentifiers != null;
    }

    @Override
    public boolean isShallow()
    {
        return this.polyLine == null && this.tags == null && this.relationIdentifiers == null;
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
        if (this.polyLine != null)
        {
            builder.append("polyLine: " + this.polyLine + ", ");
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
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier + ", polyLine="
                + this.polyLine + ", tags=" + this.tags + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public String toWkt()
    {
        if (this.polyLine == null)
        {
            return null;
        }
        return this.polyLine.toWkt();
    }

    public CompleteLine withBoundsExtendedBy(final Rectangle bounds)
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
        return this.withPolyLine(new PolyLine(locations));
    }

    @Override
    public CompleteLine withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    @Override
    public CompleteLine withPolyLine(final PolyLine polyLine)
    {
        this.polyLine = polyLine;
        this.bounds = polyLine.bounds();
        return this;
    }

    @Override
    public CompleteLine withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteLine withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }
}
