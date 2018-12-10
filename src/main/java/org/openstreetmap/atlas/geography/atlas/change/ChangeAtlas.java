package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.validators.AtlasValidator;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * Shallow atlas view that applies a set of change objects and presents the result without updating
 * the whole dataset.
 *
 * @author matthieun
 */
public class ChangeAtlas extends AbstractAtlas // NOSONAR
{
    private static final long serialVersionUID = -5741815439928958165L;

    private final Change change;
    private final Atlas source;

    private transient Rectangle bounds;
    private transient AtlasMetaData metaData;
    private transient Long numberOfNodes;
    private transient Long numberOfEdges;
    private transient Long numberOfAreas;
    private transient Long numberOfLines;
    private transient Long numberOfPoints;
    private transient Long numberOfRelations;

    public ChangeAtlas(final Atlas source, final Change change)
    {
        if (change == null)
        {
            throw new CoreException("Change cannot be null in a ChangeAtlas.");
        }
        if (source == null)
        {
            throw new CoreException("Source Atlas cannot be null in a ChangeAtlas.");
        }
        this.change = change;
        this.source = source;
        new AtlasValidator(this).validate();
    }

    @Override
    public Area area(final long identifier)
    {
        return entityFor(identifier, ItemType.AREA, () -> this.source.area(identifier),
                (sourceEntity, overrideEntity) -> new ChangeArea(this, (Area) sourceEntity,
                        (Area) overrideEntity));
    }

    @Override
    public Iterable<Area> areas()
    {
        return entitiesFor(ItemType.AREA, this::area, this.source.areas());
    }

    @Override
    public synchronized Rectangle bounds()
    {
        if (this.bounds == null)
        {
            // Stream it to make sure the "Iterable" signature is used here (vs. Located, which
            // would stack overflow).
            this.bounds = Rectangle.forLocated(Iterables.stream(this));
        }
        return this.bounds;
    }

    @Override
    public Edge edge(final long identifier)
    {
        final Edge edge = entityFor(identifier, ItemType.EDGE, () -> this.source.edge(identifier),
                (sourceEntity, overrideEntity) -> new ChangeEdge(this, (Edge) sourceEntity,
                        (Edge) overrideEntity));

        /*
         * If the edge was not found in this atlas, return null. Additionally, we then check to see
         * if this edge is missing a start or end node (which may have been removed by a
         * FeatureChange). In this case, we also want to "remove" the edge by returning null.
         */
        if (edge == null)
        {
            return null;
        }
        if (edge.start() == null || edge.end() == null)
        {
            return null;
        }
        return edge;
    }

    @Override
    public Iterable<Edge> edges()
    {
        return entitiesFor(ItemType.EDGE, this::edge, this.source.edges());
    }

    @Override
    public Line line(final long identifier)
    {
        return entityFor(identifier, ItemType.LINE, () -> this.source.line(identifier),
                (sourceEntity, overrideEntity) -> new ChangeLine(this, (Line) sourceEntity,
                        (Line) overrideEntity));
    }

    @Override
    public Iterable<Line> lines()
    {
        return entitiesFor(ItemType.LINE, this::line, this.source.lines());
    }

    @Override
    public synchronized AtlasMetaData metaData()
    {
        if (this.metaData == null)
        {
            AtlasMetaData sourceMetaData = this.source.metaData();
            if (sourceMetaData == null)
            {
                sourceMetaData = new AtlasMetaData();
            }
            final AtlasSize size = new AtlasSize(this);
            this.metaData = sourceMetaData.copyWithNewSize(size).copyWithNewOriginal(false);
        }
        return this.metaData;
    }

    @Override
    public Node node(final long identifier)
    {
        return entityFor(identifier, ItemType.NODE, () -> this.source.node(identifier),
                (sourceEntity, overrideEntity) -> new ChangeNode(this, (Node) sourceEntity,
                        (Node) overrideEntity));
    }

    @Override
    public Iterable<Node> nodes()
    {
        return entitiesFor(ItemType.NODE, this::node, this.source.nodes());
    }

    @Override
    public synchronized long numberOfAreas()
    {
        if (this.numberOfAreas == null)
        {
            this.numberOfAreas = Iterables.size(areas());
        }
        return this.numberOfAreas;
    }

    @Override
    public synchronized long numberOfEdges()
    {
        if (this.numberOfEdges == null)
        {
            this.numberOfEdges = Iterables.size(edges());
        }
        return this.numberOfEdges;
    }

    @Override
    public synchronized long numberOfLines()
    {
        if (this.numberOfLines == null)
        {
            this.numberOfLines = Iterables.size(lines());
        }
        return this.numberOfLines;
    }

    @Override
    public synchronized long numberOfNodes()
    {
        if (this.numberOfNodes == null)
        {
            this.numberOfNodes = Iterables.size(nodes());
        }
        return this.numberOfNodes;
    }

    @Override
    public synchronized long numberOfPoints()
    {
        if (this.numberOfPoints == null)
        {
            this.numberOfPoints = Iterables.size(points());
        }
        return this.numberOfPoints;
    }

    @Override
    public synchronized long numberOfRelations()
    {
        if (this.numberOfRelations == null)
        {
            this.numberOfRelations = Iterables.size(relations());
        }
        return this.numberOfRelations;
    }

    @Override
    public Point point(final long identifier)
    {
        return entityFor(identifier, ItemType.POINT, () -> this.source.point(identifier),
                (sourceEntity, overrideEntity) -> new ChangePoint(this, (Point) sourceEntity,
                        (Point) overrideEntity));
    }

    @Override
    public Iterable<Point> points()
    {
        return entitiesFor(ItemType.POINT, this::point, this.source.points());
    }

    @Override
    public Relation relation(final long identifier)
    {
        final Relation relation = entityFor(identifier, ItemType.RELATION,
                () -> this.source.relation(identifier),
                (sourceEntity, overrideEntity) -> new ChangeRelation(this, (Relation) sourceEntity,
                        (Relation) overrideEntity));

        /*
         * If the relation was not found in this atlas, return null. Additionally, we check to see
         * if the relation has no members. If so, it is considered empty and is dropped from the
         * atlas. This logic, combined with the logic in ChangeRelation.membersFor, will
         * automatically handle removing non-empty but shallow relations as well.
         */
        if (relation == null)
        {
            return null;
        }
        if (relation.members().isEmpty())
        {
            return null;
        }
        return relation;
    }

    @Override
    public Iterable<Relation> relations()
    {
        return entitiesFor(ItemType.RELATION, this::relation, this.source.relations());
    }

    /**
     * Get the {@link Iterable} of entities corresponding to the right type. This takes care of
     * surfacing only the ones not deleted, or if added or modified, the new ones.
     *
     * @param <M>
     *            The {@link AtlasEntity} subclass.
     * @param itemType
     *            The type of entity
     * @param entityForIdentifier
     *            A function that creates a new object from its identifier.
     * @param sourceEntities
     *            All the corresponding entities from the source atlas.
     * @return All the corresponding entities in this atlas.
     */
    private <M extends AtlasEntity> Iterable<M> entitiesFor(final ItemType itemType,
            final LongFunction<M> entityForIdentifier, final Iterable<M> sourceEntities)
    {
        return new MultiIterable<>(
                this.change.getFeatureChanges().stream()
                        .filter(featureChange -> featureChange.getItemType() == itemType
                                && featureChange.getChangeType() == ChangeType.ADD)
                        .map(featureChange -> entityForIdentifier
                                .apply(featureChange.getIdentifier()))
                        .filter(Objects::nonNull).collect(Collectors.toList()),
                Iterables.stream(sourceEntities)
                        .filter(entity -> !this.change.changeFor(itemType, entity.getIdentifier())
                                .isPresent())
                        .map(entity -> entityForIdentifier.apply(entity.getIdentifier()))
                        .filter(Objects::nonNull).collect());
    }

    /**
     * Build a "Change" feature for this {@link ChangeAtlas} by querying the change object for
     * matching features. Use the source atlas otherwise.
     *
     * @param <M>
     *            The type of the feature to be built. Has to extend {@link AtlasEntity}.
     * @param identifier
     *            The feature identifier
     * @param itemType
     *            The feature type
     * @param sourceSupplier
     *            A supplier function that creates the entity from the source. Can return null if
     *            the source atlas does not contain that feature.
     * @param entityConstructorFromSource
     *            A function that takes the updated feature from the {@link Change} object, and
     *            constructs a new ChangeItem from it, that attaches to this Atlas.
     * @return The ChangeItem that corresponds to that feature. Can be a ChangeNode, ChangeEdge,
     *         etc. It links back to this Atlas.
     */
    private <M extends AtlasEntity> M entityFor(final long identifier, final ItemType itemType,
            final Supplier<AtlasEntity> sourceSupplier,
            final BiFunction<AtlasEntity, AtlasEntity, M> entityConstructorFromSource)
    {
        final Optional<FeatureChange> itemChangeOption = this.change.changeFor(itemType,
                identifier);
        final AtlasEntity sourceItem = sourceSupplier.get();
        if (itemChangeOption.isPresent())
        {
            // That Entity is affected by a change
            final FeatureChange itemChange = itemChangeOption.get();
            if (ChangeType.REMOVE == itemChange.getChangeType())
            {
                return null;
            }
            else
            {
                // Create the ChangeItem from the change object (the override). The source item
                // might be null (In case of an ADD which is a create and not a modify)
                return entityConstructorFromSource.apply(sourceItem, itemChange.getReference());
            }
        }
        else
        {
            if (sourceItem != null)
            {
                // Create the ChangeItem from the untouched source; the override is null
                return entityConstructorFromSource.apply(sourceItem, null);
            }
        }
        return null;
    }
}
