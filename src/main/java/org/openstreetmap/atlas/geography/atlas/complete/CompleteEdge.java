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
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public class CompleteEdge extends Edge implements CompleteLineItem<CompleteEdge>
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;
    private long identifier;
    private PolyLine polyLine;
    private Map<String, String> tags;
    private Long startNodeIdentifier;
    private Long endNodeIdentifier;
    private Set<Long> relationIdentifiers;

    private final TagChangeDelegate tagChangeDelegate = TagChangeDelegate.newTagChangeDelegate();

    /**
     * Create a {@link CompleteEdge} from a given {@link Edge} reference. The {@link CompleteEdge}'s
     * fields will match the fields of the reference. The returned {@link CompleteEdge} will be
     * full, i.e. all of its associated fields will be non-null.
     *
     * @param edge
     *            the {@link Edge} to copy
     * @return the full {@link CompleteEdge}
     */
    public static CompleteEdge from(final Edge edge)
    {
        if (edge instanceof CompleteEdge && !((CompleteEdge) edge).isFull())
        {
            throw new CoreException("Edge parameter was a CompleteEdge but it was not full: {}",
                    edge);
        }
        return new CompleteEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags(),
                edge.start().getIdentifier(), edge.end().getIdentifier(),
                edge.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    /**
     * Create a shallow {@link CompleteEdge} from a given {@link Edge} reference. The
     * {@link CompleteEdge}'s identifier will match the identifier of the reference {@link Edge}.
     * The returned {@link CompleteEdge} will be shallow, i.e. all of its associated fields will be
     * null except for the identifier.
     *
     * @param edge
     *            the {@link Edge} to copy
     * @return the shallow {@link CompleteEdge}
     */
    public static CompleteEdge shallowFrom(final Edge edge)
    {
        if (edge.bounds() == null)
        {
            throw new CoreException("Edge parameter bounds were null");
        }
        return new CompleteEdge(edge.getIdentifier()).withBoundsExtendedBy(edge.bounds());
    }

    public CompleteEdge(final Long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final Long startNodeIdentifier,
            final Long endNodeIdentifier, final Set<Long> relationIdentifiers)
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
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
        this.relationIdentifiers = relationIdentifiers;
    }

    CompleteEdge(final long identifier)
    {
        this(identifier, null, null, null, null, null);
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
        return CompleteItemType.EDGE;
    }

    public CompleteEdge copy()
    {
        return new CompleteEdge(this.identifier, this.polyLine, this.tags, this.startNodeIdentifier,
                this.endNodeIdentifier, this.relationIdentifiers);
    }

    @Override
    public Node end()
    {
        /*
         * Note that the Node returned by this method will technically break the Located contract,
         * since it has null bounds.
         */
        return this.endNodeIdentifier == null ? null : new CompleteNode(this.endNodeIdentifier);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteEdge)
        {
            final CompleteEdge that = (CompleteEdge) other;
            return CompleteEntity.basicEqual(this, that)
                    && Objects.equals(this.asPolyLine(), that.asPolyLine())
                    && CompleteEntity.equalThroughGet(this.start(), that.start(),
                            Node::getIdentifier)
                    && CompleteEntity.equalThroughGet(this.end(), that.end(), Node::getIdentifier);
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
                && this.startNodeIdentifier != null && this.endNodeIdentifier != null
                && this.relationIdentifiers != null;
    }

    @Override
    public boolean isShallow()
    {
        return this.polyLine == null && this.startNodeIdentifier == null
                && this.endNodeIdentifier == null && this.tags == null
                && this.relationIdentifiers == null;
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
        if (this.startNodeIdentifier != null)
        {
            builder.append("startNode: " + this.startNodeIdentifier + ", ");
            builder.append(separator);
        }
        if (this.endNodeIdentifier != null)
        {
            builder.append("endNode: " + this.endNodeIdentifier + ", ");
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
    public Node start()
    {
        /*
         * Note that the Node returned by this method will technically break the Located contract,
         * since it has null bounds.
         */
        return this.startNodeIdentifier == null ? null : new CompleteNode(this.startNodeIdentifier);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier
                + ", startNodeIdentifier=" + this.startNodeIdentifier + ", endNodeIdentifier="
                + this.endNodeIdentifier + ", polyLine=" + this.polyLine + ", tags=" + this.tags
                + ", relationIdentifiers=" + this.relationIdentifiers + "]";
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

    public CompleteEdge withBoundsExtendedBy(final Rectangle bounds)
    {
        if (this.bounds == null)
        {
            this.bounds = bounds;
            return this;
        }
        this.bounds = Rectangle.forLocated(this.bounds, bounds);
        return this;
    }

    public CompleteEdge withEndNodeIdentifier(final Long endNodeIdentifier)
    {
        this.endNodeIdentifier = endNodeIdentifier;
        return this;
    }

    @Override
    public CompleteEntity withGeometry(final Iterable<Location> locations)
    {
        return this.withPolyLine(new PolyLine(locations));
    }

    @Override
    public CompleteEdge withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    @Override
    public CompleteEdge withPolyLine(final PolyLine polyLine)
    {
        this.polyLine = polyLine;
        this.bounds = polyLine.bounds();
        return this;
    }

    @Override
    public CompleteEdge withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteEdge withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    public CompleteEdge withStartNodeIdentifier(final Long startNodeIdentifier)
    {
        this.startNodeIdentifier = startNodeIdentifier;
        return this;
    }
}
