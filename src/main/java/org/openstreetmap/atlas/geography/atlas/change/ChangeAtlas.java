package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.validators.ChangeAtlasValidator;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
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
        new ChangeAtlasValidator(this).validate();
    }

    @Override
    public Area area(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Area> areas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle bounds()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Edge edge(final long identifier)
    {
        return entityFor(identifier, ItemType.EDGE, () -> this.source.edge(identifier),
                (sourceEntity, overrideEntity) -> new ChangeEdge(this, (Edge) sourceEntity,
                        (Edge) overrideEntity));
    }

    @Override
    public Iterable<Edge> edges()
    {
        return entitiesFor(ItemType.EDGE, this::edge, this.source.edges());
    }

    @Override
    public Line line(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Line> lines()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public AtlasMetaData metaData()
    {
        throw new UnsupportedOperationException();
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
    public long numberOfAreas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfEdges()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfLines()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfNodes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfPoints()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long numberOfRelations()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point point(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Point> points()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Relation relation(final long identifier)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Relation> relations()
    {
        throw new UnsupportedOperationException();
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
            final Function<Long, M> entityForIdentifier, final Iterable<M> sourceEntities)
    {
        return new MultiIterable<>(
                this.change.getFeatureChanges().stream()
                        .filter(featureChange -> featureChange.getItemType() == itemType
                                && featureChange.getChangeType() == ChangeType.ADD)
                        .map(featureChange -> entityForIdentifier
                                .apply(featureChange.getIdentifier()))
                        .filter(entity -> entity != null).collect(Collectors.toList()),
                Iterables.stream(sourceEntities)
                        .filter(entity -> !this.change.changeFor(itemType, entity.getIdentifier())
                                .isPresent())
                        .map(entity -> entityForIdentifier.apply(entity.getIdentifier()))
                        .filter(entity -> entity != null).collect());
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
