package org.openstreetmap.atlas.geography;

import java.util.Objects;

import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.common.collect.Iterables;

/**
 * Snap a {@link Location} to a {@link PolyLine}.
 *
 * @author matthieun
 * @author bbreithaupt
 */
public class Snapper
{
    /**
     * A snapped location on a shape.
     *
     * @author matthieun
     */
    public static class SnappedLocation extends Location implements Comparable<SnappedLocation>
    {
        private static final long serialVersionUID = -3283158797347353372L;

        private final Location origin;
        private final PolyLine target;

        public SnappedLocation(final Location origin, final Location snapped, final PolyLine target)
        {
            super(snapped.asConcatenation());
            this.origin = origin;
            this.target = target;
        }

        @Override
        public int compareTo(final SnappedLocation other)
        {
            if (getDistance().isLessThan(other.getDistance()))
            {
                return -1;
            }
            else if (getDistance().equals(other.getDistance()))
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof SnappedLocation)
            {
                return this.origin.equals(((SnappedLocation) other).getOrigin())
                        && this.target.equals(((SnappedLocation) other).getTarget());
            }
            if (other instanceof Location)
            {
                return super.equals(other);
            }
            return false;
        }

        /**
         * @return The distance between the origin and the snapped {@link Location}
         */
        public Distance getDistance()
        {
            return this.origin.distanceTo(this);
        }

        public Location getOrigin()
        {
            return this.origin;
        }

        public PolyLine getTarget()
        {
            return this.target;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.origin, this.target);
        }
    }

    /**
     * Snap a point on a {@link PolyLine}
     *
     * @param origin
     *            The point to snap
     * @param shape
     *            The {@link PolyLine} to snap to
     * @return The resulting {@link SnappedLocation}
     */
    public SnappedLocation snap(final Location origin, final Iterable<? extends Location> shape)
    {
        if (shape instanceof Segment)
        {
            final Segment target = (Segment) shape;
            return snapSegment(origin, target);
        }
        else if (Iterables.size(shape) > 1)
        {
            final PolyLine target;
            if (shape instanceof PolyLine)
            {
                target = (PolyLine) shape;
            }
            else if (shape instanceof Polygon)
            {
                target = (Polygon) shape;
            }
            else
            {
                target = new PolyLine(shape);
            }
            SnappedLocation best = null;
            for (final Segment segment : target.segments())
            {
                final SnappedLocation candidate = snap(origin, segment);
                if (best == null || candidate.getDistance().isLessThan(best.getDistance()))
                {
                    best = candidate;
                }
            }
            // Return a SnappedLocation with the full shape
            return new SnappedLocation(origin, best, target);
        }
        else if (Iterables.size(shape) == 1)
        {
            // We have a single location in the Iterable
            final Location target = shape.iterator().next();
            return new SnappedLocation(origin, target, new PolyLine(target));
        }
        return null;
    }

    public SnappedLocation snap(final Location origin, final MultiPolygon shape)
    {
        SnappedLocation best = null;
        for (final Polygon member : new MultiIterable<>(shape.outers(), shape.inners()))
        {
            final SnappedLocation candidate = snap(origin, member);
            if (best == null || candidate.getDistance().isLessThan(best.getDistance()))
            {
                best = candidate;
            }
        }
        return best;
    }

    private SnappedLocation snapSegment(final Location origin, final Segment shape)
    {
        // Use the dot product to determine if the snapped point is within the segment, or at
        // the edge points
        final Segment variable = new Segment(shape.start(), origin);
        final double dotProduct = shape.dotProduct(variable);
        if (dotProduct <= 0)
        {
            return new SnappedLocation(origin, shape.start(), shape);
        }
        // Here, NOSONAR to avoid "Collections should not be passed as arguments to their own
        // methods (squid:S2114)"
        // It is triggered because Segment is also a collection.
        if (dotProduct >= shape.dotProduct(shape)) // NOSONAR
        {
            return new SnappedLocation(origin, shape.end(), shape);
        }
        // Find the point in the middle.
        // Inspired from http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html#step5
        // The angle between the target and variable segment is alpha
        final double cosAlpha = dotProduct
                / (shape.dotProductLength() * variable.dotProductLength());
        // Cos Alpha is also defined as (offset distance on target) / (variable's length)
        final double offsetDistance = cosAlpha * variable.dotProductLength();
        final double latitudeAsDm7 = shape.start().getLatitude().asDm7()
                + offsetDistance / shape.dotProductLength() * shape.latitudeSpan();
        final double longitudeAsDm7 = shape.start().getLongitude().asDm7()
                + offsetDistance / shape.dotProductLength() * shape.longitudeSpan();
        final Location snapped = new Location(Latitude.dm7(Math.round(latitudeAsDm7)),
                Longitude.dm7(Math.round(longitudeAsDm7)));
        return new SnappedLocation(origin, snapped, shape);
    }
}
