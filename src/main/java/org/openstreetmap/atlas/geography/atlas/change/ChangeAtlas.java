package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
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
public class ChangeAtlas extends AbstractAtlas
{
    private static final long serialVersionUID = -5741815439928958165L;

    public ChangeAtlas(final Atlas source, final Change change)
    {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
}
