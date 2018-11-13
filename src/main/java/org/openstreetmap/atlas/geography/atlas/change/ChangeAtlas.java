package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

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
        return buildFor(identifier, ItemType.EDGE, () -> this.source.edge(identifier),
                entity -> new ChangeEdge(this, (Edge) entity));
    }

    @Override
    public Iterable<Edge> edges()
    {
        throw new UnsupportedOperationException();
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
        return buildFor(identifier, ItemType.NODE, () -> this.source.node(identifier),
                entity -> new ChangeNode(this, (Node) entity));
    }

    @Override
    public Iterable<Node> nodes()
    {
        throw new UnsupportedOperationException();
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

    private <T extends AtlasEntity> T buildFor(final long identifier, final ItemType itemType,
            final Supplier<AtlasEntity> sourceSupplier,
            final Function<AtlasEntity, T> entityConstructorFromSource)
    {
        final Optional<FeatureChange> itemChangeOption = this.change.changeFor(itemType,
                identifier);
        if (itemChangeOption.isPresent())
        {
            // That Edge is affected by a change
            final FeatureChange itemChange = itemChangeOption.get();
            if (ChangeType.REMOVE == itemChange.getChangeType())
            {
                return null;
            }
            else
            {
                // Create the ChangeItem from the change object
                return entityConstructorFromSource.apply(itemChange.getReference());
            }
        }
        else
        {
            final AtlasEntity sourceItem = sourceSupplier.get();
            if (sourceItem != null)
            {
                // Create the ChangeItem from the untouched source
                return entityConstructorFromSource.apply(sourceItem);
            }
        }
        return null;
    }
}
