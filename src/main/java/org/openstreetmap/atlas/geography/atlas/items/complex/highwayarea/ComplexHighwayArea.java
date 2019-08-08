package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * A complex entity of highway areas.
 *
 * @author mgostintsev
 * @author isabellehillberg
 * @author cstaylor
 */
public class ComplexHighwayArea extends ComplexEntity
{
    private static final long serialVersionUID = 441824709133762710L;

    private static final Logger logger = LoggerFactory.getLogger(ComplexHighwayArea.class);

    private PolyLine boundary;

    private final NavigableSet<Long> visitedEdgeIdentifiers = new TreeSet<>();

    protected ComplexHighwayArea(final ComplexHighwayAreaHelper helper)
    {
        super(helper.getSourceEdge());
        try
        {
            if (helper.getException() != null)
            {
                throw helper.getException();
            }
            this.boundary = helper.getBoundary();
            this.visitedEdgeIdentifiers.addAll(helper.getVisitedEdgeIdentifiers());
            if (isSelfIntersecting())
            {
                throw new CoreException("Self-intersecting geometry");
            }
            if (isZeroSized())
            {
                throw new CoreException("Zero-sized area");
            }
        }
        catch (final Exception oops)
        {
            logger.warn("Unable to create ComplexHighwayArea from {}", helper.getSourceEdge(),
                    oops);
            setInvalidReason("Couldn't create ComplexHighwayArea", oops);
        }
    }

    protected ComplexHighwayArea(final Edge edge)
    {
        this(new ComplexHighwayAreaHelper(edge));
    }

    public PolyLine getHighwayAreaBoundary()
    {
        return this.boundary;
    }

    /**
     * Returns the atlas identifiers of all edges that were navigated when creating this highway
     * area
     *
     * @return a sorted list of atlas edge identifiers that we visited when creating this highway
     *         area
     */
    public NavigableSet<Long> getVisitedEdgeIdentifiers()
    {
        return this.visitedEdgeIdentifiers;
    }

    public boolean isSelfIntersecting()
    {
        return boundaryAsPolygon().selfIntersects();
    }

    public boolean isZeroSized()
    {
        return boundaryAsPolygon().surface().equals(Surface.MINIMUM);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this).add("osm", getSource().getOsmIdentifier())
                .add("atlas", getSource().getIdentifier()).add("boundary", this.boundary)
                .toString();
    }

    private Polygon boundaryAsPolygon()
    {
        if (!isValid())
        {
            throw new CoreException("Highway Area is invalid {}", getOsmIdentifier());
        }
        final List<Location> locations = new ArrayList<>();
        locations.addAll(this.boundary);
        return new Polygon(locations);
    }
}
