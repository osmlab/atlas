package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.common.collect.Iterables;

/**
 * Snap a {@link Location} to a {@link PolyLine}.
 *
 * @author matthieun
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
            return getDistance().isLessThan(other.getDistance()) ? -1
                    : getDistance().equals(other.getDistance()) ? 0 : 1;
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
            // Use the dot product to determine if the snapped point is within the segment, or at
            // the edge points
            final Segment variable = new Segment(target.start(), origin);
            final double dotProduct = target.dotProduct(variable);
            if (dotProduct <= 0)
            {
                return new SnappedLocation(origin, target.start(), target);
            }
            if (dotProduct >= target.dotProduct(target))
            {
                return new SnappedLocation(origin, target.end(), target);
            }
            // Find the point in the middle.
            // Inspired from http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html#step5
            // The angle between the target and variable segment is alpha
            final double cosAlpha = dotProduct
                    / (target.dotProductLength() * variable.dotProductLength());
            // Cos Alpha is also defined as (offset distance on target) / (variable's length)
            final double offsetDistance = cosAlpha * variable.dotProductLength();
            final double latitudeAsDm7 = target.start().getLatitude().asDm7()
                    + offsetDistance / target.dotProductLength() * target.latitudeSpan();
            final double longitudeAsDm7 = target.start().getLongitude().asDm7()
                    + offsetDistance / target.dotProductLength() * target.longitudeSpan();
            final Location snapped = new Location(Latitude.dm7(Math.round(latitudeAsDm7)),
                    Longitude.dm7(Math.round(longitudeAsDm7)));
            return new SnappedLocation(origin, snapped, target);
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
}
