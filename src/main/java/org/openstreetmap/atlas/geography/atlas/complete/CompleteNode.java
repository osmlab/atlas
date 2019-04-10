package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener.TagChangeListener;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Node} that may contain its own altered data. At scale, use at your own risk.
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public class CompleteNode extends Node implements CompleteLocationItem<CompleteNode>
{
    private static final long serialVersionUID = -8229589987121555419L;

    private Rectangle bounds;
    private long identifier;
    private Location location;
    private Map<String, String> tags;
    private SortedSet<Long> inEdgeIdentifiers;
    private SortedSet<Long> outEdgeIdentifiers;
    private Set<Long> relationIdentifiers;

    //private TagChangeDelegate tagChangeDelegate = TagChangeDelegate.newTagChangeDelegate();

    /**
     * Create a {@link CompleteNode} from a given {@link Node} reference. The {@link CompleteNode}'s
     * fields will match the fields of the reference. The returned {@link CompleteNode} will be
     * full, i.e. all of its associated fields will be non-null.
     *
     * @param node
     *            the {@link Node} to copy
     * @return the full {@link CompleteNode}
     */
    public static CompleteNode from(final Node node)
    {
        return new CompleteNode(node.getIdentifier(), node.getLocation(), node.getTags(),
                node.inEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                node.outEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                node.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    /**
     * Create a shallow {@link CompleteNode} from a given {@link Node} reference. The
     * {@link CompleteNode}'s identifier will match the identifier of the reference {@link Node}.
     * The returned {@link CompleteNode} will be shallow, i.e. all of its associated fields will be
     * null except for the identifier.
     *
     * @param node
     *            the {@link Node} to copy
     * @return the shallow {@link CompleteNode}
     */
    public static CompleteNode shallowFrom(final Node node)
    {
        return new CompleteNode(node.getIdentifier()).withBoundsExtendedBy(node.bounds());
    }

    CompleteNode(final long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    public CompleteNode(final Long identifier, final Location location,
            final Map<String, String> tags, final SortedSet<Long> inEdgeIdentifiers,
            final SortedSet<Long> outEdgeIdentifiers, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.bounds = location != null ? location.bounds() : null;

        this.identifier = identifier;
        this.location = location;
        this.tags = tags;
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteNode)
        {
            final CompleteNode that = (CompleteNode) other;
            return CompleteEntity.basicEqual(this, that)
                    && Objects.equals(this.getLocation(), that.getLocation())
                    && Objects.equals(this.inEdges(), that.inEdges())
                    && Objects.equals(this.outEdges(), that.outEdges());
        }
        return false;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Location getLocation()
    {
        return this.location;
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
    public SortedSet<Edge> inEdges()
    {
        /*
         * Note that the Edges returned by this method will technically break the Located contract,
         * since they have null bounds.
         */
        return this.inEdgeIdentifiers == null ? null
                : this.inEdgeIdentifiers.stream().map(CompleteEdge::new)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public boolean isShallow()
    {
        return this.location == null && this.inEdgeIdentifiers == null
                && this.outEdgeIdentifiers == null && this.tags == null
                && this.relationIdentifiers == null;
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        /*
         * Note that the Edges returned by this method will technically break the Located contract,
         * since they have null bounds.
         */
        return this.outEdgeIdentifiers == null ? null
                : this.outEdgeIdentifiers.stream().map(CompleteEdge::new)
                        .collect(Collectors.toCollection(TreeSet::new));
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
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier
                + ", inEdgeIdentifiers=" + this.inEdgeIdentifiers + ", outEdgeIdentifiers="
                + this.outEdgeIdentifiers + ", location=" + this.location + ", tags=" + this.tags
                + ", relationIdentifiers=" + this.relationIdentifiers + "]";
    }

    public CompleteNode withBoundsExtendedBy(final Rectangle bounds)
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
    public CompleteNode withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public CompleteNode withInEdgeIdentifierExtra(final Long extraInEdgeIdentifier)
    {
        this.inEdgeIdentifiers.add(extraInEdgeIdentifier);
        return this;
    }

    public CompleteNode withInEdgeIdentifierLess(final Long lessInEdgeIdentifier)
    {
        this.inEdgeIdentifiers.remove(lessInEdgeIdentifier);
        return this;
    }

    public CompleteNode withInEdgeIdentifierReplaced(final Long beforeInEdgeIdentifier,
            final Long afterInEdgeIdentifier)
    {
        return this.withInEdgeIdentifierLess(beforeInEdgeIdentifier)
                .withInEdgeIdentifierExtra(afterInEdgeIdentifier);
    }

    public CompleteNode withInEdgeIdentifiers(final SortedSet<Long> inEdgeIdentifiers)
    {
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        return this;
    }

    public CompleteNode withInEdges(final Set<Edge> inEdges)
    {
        this.inEdgeIdentifiers = inEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
        return this;
    }

    @Override
    public CompleteNode withLocation(final Location location)
    {
        this.location = location;
        this.bounds = location.bounds();
        return this;
    }

    public CompleteNode withOutEdgeIdentifierExtra(final Long extraOutEdgeIdentifier)
    {
        this.outEdgeIdentifiers.add(extraOutEdgeIdentifier);
        return this;
    }

    public CompleteNode withOutEdgeIdentifierLess(final Long lessOutEdgeIdentifier)
    {
        this.outEdgeIdentifiers.remove(lessOutEdgeIdentifier);
        return this;
    }

    public CompleteNode withOutEdgeIdentifierReplaced(final Long beforeOutEdgeIdentifier,
            final Long afterOutEdgeIdentifier)
    {
        return this.withOutEdgeIdentifierLess(beforeOutEdgeIdentifier)
                .withOutEdgeIdentifierExtra(afterOutEdgeIdentifier);
    }

    public CompleteNode withOutEdgeIdentifiers(final SortedSet<Long> outEdgeIdentifiers)
    {
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        return this;
    }

    public CompleteNode withOutEdges(final Set<Edge> outEdges)
    {
        this.outEdgeIdentifiers = outEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
        return this;
    }

    @Override
    public CompleteNode withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteNode withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    @Override
    public void addTagChangeListener(final TagChangeListener tagChangeListener)
    {
        //tagChangeDelegate.addTagChangeListener(tagChangeListener);
    }

    @Override
    public void fireTagChangeEvent(final TagChangeEvent tagChangeEvent)
    {
        //tagChangeDelegate.fireTagChangeEvent(tagChangeEvent);
    }

    @Override
    public void removeTagChangeListeners()
    {
        //tagChangeDelegate.removeTagChangeListeners();
    }

    @Override
    public void setTags(final Map<String, String> tags)
    {
        this.tags = tags;
    }

    @Override
    public CompleteItemType completeItemType()
    {
        return CompleteItemType.NODE;
    }
}
