package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Independent {@link Node} that may contain its own altered data. At scale, use at your own risk.
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public class CompleteNode extends Node implements CompleteLocationItem<CompleteNode>
{
    private static final long serialVersionUID = -8229589987121555419L;

    private final TagChangeDelegate tagChangeDelegate = TagChangeDelegate.newTagChangeDelegate();

    private Rectangle bounds;
    private long identifier;
    private Location location;
    private Map<String, String> tags;
    private SortedSet<Long> inEdgeIdentifiers;
    private SortedSet<Long> outEdgeIdentifiers;
    private Set<Long> explicitlyExcludedInEdgeIdentifiers;
    private Set<Long> explicitlyExcludedOutEdgeIdentifiers;
    private Set<Long> relationIdentifiers;

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
        if (node instanceof CompleteNode && !((CompleteNode) node).isFull())
        {
            throw new CoreException("Node parameter was a CompleteNode but it was not full: {}",
                    node);
        }
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
        if (node.bounds() == null)
        {
            throw new CoreException("Node parameter bounds were null");
        }
        return new CompleteNode(node.getIdentifier()).withBoundsExtendedBy(node.bounds());
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
        this.explicitlyExcludedInEdgeIdentifiers = new HashSet<>();
        this.explicitlyExcludedOutEdgeIdentifiers = new HashSet<>();
        this.relationIdentifiers = relationIdentifiers;
    }

    CompleteNode(final long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    @Override
    public void addTagChangeListener(final TagChangeListener tagChangeListener)
    {
        this.tagChangeDelegate.addTagChangeListener(tagChangeListener);
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    @Override
    public CompleteItemType completeItemType()
    {
        return CompleteItemType.NODE;
    }

    public CompleteNode copy()
    {
        return new CompleteNode(this.identifier, this.location, this.tags, this.inEdgeIdentifiers,
                this.outEdgeIdentifiers, this.relationIdentifiers);
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

    public Set<Long> explicitlyExcludedInEdgeIdentifiers()
    {
        return this.explicitlyExcludedInEdgeIdentifiers;
    }

    public Set<Long> explicitlyExcludedOutEdgeIdentifiers()
    {
        return this.explicitlyExcludedOutEdgeIdentifiers;
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

    public Set<Long> inEdgeIdentifiers()
    {
        return this.inEdgeIdentifiers;
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
    public boolean isFull()
    {
        return this.bounds != null && this.location != null && this.tags != null
                && this.inEdgeIdentifiers != null && this.outEdgeIdentifiers != null
                && this.relationIdentifiers != null;
    }

    @Override
    public boolean isShallow()
    {
        return this.location == null && this.inEdgeIdentifiers == null
                && this.outEdgeIdentifiers == null && this.tags == null
                && this.relationIdentifiers == null;
    }

    public Set<Long> outEdgeIdentifiers()
    {
        return this.outEdgeIdentifiers;
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
    public String prettify(final PrettifyStringFormat format, final boolean truncate)
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
        if (this.location != null)
        {
            builder.append("geometry: " + this.location + ", ");
            builder.append(separator);
        }
        if (this.tags != null)
        {
            builder.append("tags: " + new TreeMap<>(this.tags) + ", ");
            builder.append(separator);
        }
        if (this.inEdgeIdentifiers != null)
        {
            builder.append("inEdges: " + this.inEdgeIdentifiers + ", ");
            builder.append(separator);
        }
        if (this.explicitlyExcludedInEdgeIdentifiers != null
                && !this.explicitlyExcludedInEdgeIdentifiers.isEmpty())
        {
            builder.append("explicitlyExcludedInEdges: "
                    + new TreeSet<>(this.explicitlyExcludedInEdgeIdentifiers) + ", ");
            builder.append(separator);
        }
        if (this.outEdgeIdentifiers != null)
        {
            builder.append("outEdges: " + this.outEdgeIdentifiers + ", ");
            builder.append(separator);
        }
        if (this.explicitlyExcludedOutEdgeIdentifiers != null
                && !this.explicitlyExcludedOutEdgeIdentifiers.isEmpty())
        {
            builder.append("explicitlyExcludedOutEdges: "
                    + new TreeSet<>(this.explicitlyExcludedOutEdgeIdentifiers) + ", ");
            builder.append(separator);
        }
        if (this.relationIdentifiers != null)
        {
            builder.append("parentRelations: " + new TreeSet<>(this.relationIdentifiers) + ", ");
            builder.append(separator);
        }
        if (this.bounds != null)
        {
            builder.append("bounds: " + this.bounds.toWkt() + ", ");
            builder.append(separator);
        }
        builder.append("]");

        return builder.toString();
    }

    @Override
    public Set<Long> relationIdentifiers()
    {
        return this.relationIdentifiers;
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

    public void setExplicitlyExcludedInEdgeIdentifiers(final Set<Long> edges)
    {
        this.explicitlyExcludedInEdgeIdentifiers = edges;
    }

    public void setExplicitlyExcludedOutEdgeIdentifiers(final Set<Long> edges)
    {
        this.explicitlyExcludedOutEdgeIdentifiers = edges;
    }

    @Override
    public void setTags(final Map<String, String> tags)
    {
        this.tags = tags != null ? new HashMap<>(tags) : null;
    }

    @Override
    public JsonObject toJson()
    {
        final JsonObject nodeObject = super.toJson();

        final JsonArray inEdgeIdentifiersArray = new JsonArray();
        for (final Long inEdgeIdentifier : new TreeSet<>(this.inEdgeIdentifiers))
        {
            inEdgeIdentifiersArray.add(new JsonPrimitive(inEdgeIdentifier));
        }
        final JsonArray outEdgeIdentifiersArray = new JsonArray();
        for (final Long outEdgeIdentifier : new TreeSet<>(this.outEdgeIdentifiers))
        {
            outEdgeIdentifiersArray.add(new JsonPrimitive(outEdgeIdentifier));
        }
        nodeObject.add("inEdges", inEdgeIdentifiersArray);
        nodeObject.add("outEdges", outEdgeIdentifiersArray);

        return nodeObject;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier
                + ", inEdgeIdentifiers=" + this.inEdgeIdentifiers + ", outEdgeIdentifiers="
                + this.outEdgeIdentifiers + ", location=" + this.location + ", tags=" + this.tags
                + ", relationIdentifiers=" + this.relationIdentifiers + "]";
    }

    @Override
    public String toWkt()
    {
        if (this.location == null)
        {
            return null;
        }
        return this.location.toWkt();
    }

    public CompleteNode withAddedInEdgeIdentifier(final Long inEdgeIdentifier)
    {
        this.inEdgeIdentifiers.add(inEdgeIdentifier);
        return this;
    }

    public CompleteNode withAddedOutEdgeIdentifier(final Long inEdgeIdentifier)
    {
        this.outEdgeIdentifiers.add(inEdgeIdentifier);
        return this;
    }

    @Override
    public CompleteNode withAddedRelationIdentifier(final Long relationIdentifier)
    {
        this.relationIdentifiers.add(relationIdentifier);
        return this;
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
    public CompleteEntity withGeometry(final Iterable<Location> locations)
    {
        if (!locations.iterator().hasNext())
        {
            throw new CoreException("Cannot interpret empty Iterable as a Location");
        }
        return this.withLocation(locations.iterator().next());
    }

    @Override
    public CompleteNode withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public CompleteNode withInEdgeIdentifiers(final SortedSet<Long> inEdgeIdentifiers)
    {
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        return this;
    }

    public CompleteNode withInEdgeIdentifiersAndSource(final SortedSet<Long> inEdgeIdentifiers,
            final Node source)
    {
        final Set<Long> sourceIdentifiers = source.inEdges().stream().map(Edge::getIdentifier)
                .collect(Collectors.toSet());
        final Set<Long> excludedBasedOnSource = com.google.common.collect.Sets
                .difference(sourceIdentifiers, inEdgeIdentifiers);
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        this.explicitlyExcludedInEdgeIdentifiers.addAll(excludedBasedOnSource);
        return this;
    }

    public CompleteNode withInEdges(final Set<Edge> inEdges)
    {
        this.inEdgeIdentifiers = inEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
        return this;
    }

    public CompleteNode withInEdgesAndSource(final Set<Edge> inEdges, final Node source)
    {
        return withInEdgeIdentifiersAndSource(inEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new)), source);
    }

    @Override
    public CompleteNode withLocation(final Location location)
    {
        this.location = location;
        if (this.location != null)
        {
            this.bounds = location.bounds();
        }
        return this;
    }

    public CompleteNode withOutEdgeIdentifiers(final SortedSet<Long> outEdgeIdentifiers)
    {
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        return this;
    }

    public CompleteNode withOutEdgeIdentifiersAndSource(final SortedSet<Long> outEdgeIdentifiers,
            final Node source)
    {
        final Set<Long> sourceIdentifiers = source.outEdges().stream().map(Edge::getIdentifier)
                .collect(Collectors.toSet());
        final Set<Long> excludedBasedOnSource = com.google.common.collect.Sets
                .difference(sourceIdentifiers, outEdgeIdentifiers);
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        this.explicitlyExcludedOutEdgeIdentifiers.addAll(excludedBasedOnSource);
        return this;
    }

    public CompleteNode withOutEdges(final Set<Edge> outEdges)
    {
        this.outEdgeIdentifiers = outEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
        return this;
    }

    public CompleteNode withOutEdgesAndSource(final Set<Edge> outEdges, final Node source)
    {
        return withOutEdgeIdentifiersAndSource(outEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new)), source);
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

    public CompleteNode withRemovedInEdgeIdentifier(final Long inEdgeIdentifier)
    {
        this.inEdgeIdentifiers.remove(inEdgeIdentifier);
        this.explicitlyExcludedInEdgeIdentifiers.add(inEdgeIdentifier);
        return this;
    }

    public CompleteNode withRemovedOutEdgeIdentifier(final Long outEdgeIdentifier)
    {
        this.outEdgeIdentifiers.remove(outEdgeIdentifier);
        this.explicitlyExcludedOutEdgeIdentifiers.add(outEdgeIdentifier);
        return this;
    }

    @Override
    public CompleteNode withRemovedRelationIdentifier(final Long relationIdentifier)
    {
        this.relationIdentifiers = this.relationIdentifiers.stream()
                .filter(keepId -> keepId != relationIdentifier.longValue())
                .collect(Collectors.toSet());
        return this;
    }

    public CompleteNode withReplacedInEdgeIdentifier(final Long beforeInEdgeIdentifier,
            final Long afterInEdgeIdentifier)
    {
        return this.withRemovedInEdgeIdentifier(beforeInEdgeIdentifier)
                .withAddedInEdgeIdentifier(afterInEdgeIdentifier);
    }

    public CompleteNode withReplacedOutEdgeIdentifier(final Long beforeOutEdgeIdentifier,
            final Long afterOutEdgeIdentifier)
    {
        return this.withRemovedOutEdgeIdentifier(beforeOutEdgeIdentifier)
                .withAddedOutEdgeIdentifier(afterOutEdgeIdentifier);
    }
}
