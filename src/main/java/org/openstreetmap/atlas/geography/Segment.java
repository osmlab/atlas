package org.openstreetmap.atlas.geography;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PolyLine} made of two {@link Location}s
 *
 * @author matthieun
 */
public class Segment extends PolyLine
{
    private static final Logger logger = LoggerFactory.getLogger(Segment.class);
    private static final long serialVersionUID = -5796676985841139897L;

    /**
     * Convenience method to gather all {@link Location}s for a list of segments.
     *
     * @param segments
     *            target segments
     * @return a list of {@link Location}s for the given segments
     */
    public static List<Location> asList(final Iterable<Segment> segments)
    {
        final List<Location> result = new ArrayList<>();
        Iterables.stream(segments).forEach(segment ->
        {
            if (result.isEmpty() || !result.get(result.size() - 1).equals(segment.start()))
            {
                result.add(segment.start());
            }
            result.add(segment.end());
        });
        return result;
    }

    /**
     * Convenience method to speed up the construction of the parent {@link PolyLine}.
     */
    private static List<Location> asList(final Location start, final Location end)
    {
        final List<Location> result = new ArrayList<>();
        result.add(start);
        result.add(end);
        return result;
    }

    public Segment(final Location start, final Location end)
    {
        super(asList(start, end));
    }

    public Location end()
    {
        return this.last();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Segment)
        {
            final Segment that = (Segment) other;
            return this.start().equals(that.start()) && this.end().equals(that.end());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.end() == null ? 0 : this.end().hashCode());
        result = prime * result + (this.start() == null ? 0 : this.start().hashCode());
        return result;
    }

    /**
     * @return The {@link Segment}'s {@link Heading}. In case the segment is the same start and end
     *         locations, then the result is empty.
     */
    public Optional<Heading> heading()
    {
        if (this.isPoint())
        {
            logger.warn(
                    "Cannot compute a segment's heading when the segment is a point with same start and end {}",
                    this.start());
            return Optional.empty();
        }
        return Optional.of(this.start().headingTo(this.end()));
    }

    /**
     * Intersection of two segments
     *
     * @param that
     *            The other segment to intersect
     * @return The intersection point if any, null otherwise
     * @see "http://stackoverflow.com/a/1968345/1558687"
     */
    public Location intersection(final Segment that)
    {
        final double p0X = this.start().getLongitude().asDegrees();
        final double p0Y = this.start().getLatitude().asDegrees();
        final double p1X = this.end().getLongitude().asDegrees();
        final double p1Y = this.end().getLatitude().asDegrees();
        final double p2X = that.start().getLongitude().asDegrees();
        final double p2Y = that.start().getLatitude().asDegrees();
        final double p3X = that.end().getLongitude().asDegrees();
        final double p3Y = that.end().getLatitude().asDegrees();

        final double s1X;
        final double s1Y;
        final double s2X;
        final double s2Y;
        s1X = p1X - p0X;
        s1Y = p1Y - p0Y;
        s2X = p3X - p2X;
        s2Y = p3Y - p2Y;

        final double sValue;
        final double tValue;
        sValue = (-s1Y * (p0X - p2X) + s1X * (p0Y - p2Y)) / (-s2X * s1Y + s1X * s2Y);
        tValue = (s2X * (p0Y - p2Y) - s2Y * (p0X - p2X)) / (-s2X * s1Y + s1X * s2Y);

        if (sValue >= 0 && sValue <= 1 && tValue >= 0 && tValue <= 1)
        {
            // Collision detected
            return new Location(Latitude.degrees(p0Y + tValue * s1Y),
                    Longitude.degrees(p0X + tValue * s1X));
        }
        // No collision
        return null;
    }

    /**
     * A fast method to test if two segments intersect
     *
     * @param that
     *            The other {@link Segment} to test with
     * @return True if this segment intersects that segment
     * @see "http://www.java-gaming.org/index.php?topic=22590.0"
     */
    public boolean intersects(final Segment that)
    {
        final long xAxis1 = this.start().getLongitude().asDm7();
        final long yAxis1 = this.start().getLatitude().asDm7();
        final long xAxis2 = this.end().getLongitude().asDm7();
        final long yAxis2 = this.end().getLatitude().asDm7();
        final long xAxis3 = that.start().getLongitude().asDm7();
        final long yAxis3 = that.start().getLatitude().asDm7();
        final long xAxis4 = that.end().getLongitude().asDm7();
        final long yAxis4 = that.end().getLatitude().asDm7();

        // Return false if either of the lines have zero length
        if (xAxis1 == xAxis2 && yAxis1 == yAxis2 || xAxis3 == xAxis4 && yAxis3 == yAxis4)
        {
            return false;
        }
        // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic
        // "in Graphics Gems III" book (http://www.graphicsgems.org/)
        final long axAxis = xAxis2 - xAxis1;
        final long ayAxis = yAxis2 - yAxis1;
        final long bxAxis = xAxis3 - xAxis4;
        final long byAxis = yAxis3 - yAxis4;
        final long cxAxis = xAxis1 - xAxis3;
        final long cyAxis = yAxis1 - yAxis3;

        try
        {
            final long alphaNumerator = Math.subtractExact(byAxis * cxAxis, bxAxis * cyAxis);
            final long commonDenominator = Math.subtractExact(ayAxis * bxAxis, axAxis * byAxis);
            if (commonDenominator > 0)
            {
                if (alphaNumerator < 0 || alphaNumerator > commonDenominator)
                {
                    return false;
                }
            }
            else if (commonDenominator < 0)
            {
                if (alphaNumerator > 0 || alphaNumerator < commonDenominator)
                {
                    return false;
                }
            }
            final long betaNumerator = Math.subtractExact(axAxis * cyAxis, ayAxis * cxAxis);
            if (commonDenominator > 0)
            {
                if (betaNumerator < 0 || betaNumerator > commonDenominator)
                {
                    return false;
                }
            }
            else if (commonDenominator < 0)
            {
                if (betaNumerator > 0 || betaNumerator < commonDenominator)
                {
                    return false;
                }
            }
            if (commonDenominator == 0)
            {
                // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
                // The
                // lines are parallel. Check if they're collinear.
                // see "http://mathworld.wolfram.com/Collinear.html"
                final long collinearityTestForP3 = xAxis1 * (yAxis2 - yAxis3)
                        + xAxis2 * (yAxis3 - yAxis1) + xAxis3 * (yAxis1 - yAxis2);
                // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is
                // parallel with p3-p4
                if (collinearityTestForP3 == 0)
                {
                    // The lines are collinear. Now check if they overlap.
                    if (xAxis1 >= xAxis3 && xAxis1 <= xAxis4 || xAxis1 <= xAxis3 && xAxis1 >= xAxis4
                            || xAxis2 >= xAxis3 && xAxis2 <= xAxis4
                            || xAxis2 <= xAxis3 && xAxis2 >= xAxis4
                            || xAxis3 >= xAxis1 && xAxis3 <= xAxis2
                            || xAxis3 <= xAxis1 && xAxis3 >= xAxis2)
                    {
                        if (yAxis1 >= yAxis3 && yAxis1 <= yAxis4
                                || yAxis1 <= yAxis3 && yAxis1 >= yAxis4
                                || yAxis2 >= yAxis3 && yAxis2 <= yAxis4
                                || yAxis2 <= yAxis3 && yAxis2 >= yAxis4
                                || yAxis3 >= yAxis1 && yAxis3 <= yAxis2
                                || yAxis3 <= yAxis1 && yAxis3 >= yAxis2)
                        {
                            return true;
                        }
                    }
                }
                return false;
            }
            return true;
        }
        catch (final ArithmeticException overflow)
        {
            return this.intersectsApproximate(that);
        }
    }

    /**
     * Implements the same function as intersects but with doubles to avoid overlflow issues. Should
     * only happen for cross world intersection.
     * 
     * @param that
     * @return
     */
    private boolean intersectsApproximate(final Segment that)
    {
        final double xAxis1 = this.start().getLongitude().asDegrees();
        final double yAxis1 = this.start().getLatitude().asDegrees();
        final double xAxis2 = this.end().getLongitude().asDegrees();
        final double yAxis2 = this.end().getLatitude().asDegrees();
        final double xAxis3 = that.start().getLongitude().asDegrees();
        final double yAxis3 = that.start().getLatitude().asDegrees();
        final double xAxis4 = that.end().getLongitude().asDegrees();
        final double yAxis4 = that.end().getLatitude().asDegrees();

        // Return false if either of the lines have zero length
        if (xAxis1 == xAxis2 && yAxis1 == yAxis2 || xAxis3 == xAxis4 && yAxis3 == yAxis4)
        {
            return false;
        }
        // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic
        // "in Graphics Gems III" book (http://www.graphicsgems.org/)
        final double axAxis = xAxis2 - xAxis1;
        final double ayAxis = yAxis2 - yAxis1;
        final double bxAxis = xAxis3 - xAxis4;
        final double byAxis = yAxis3 - yAxis4;
        final double cxAxis = xAxis1 - xAxis3;
        final double cyAxis = yAxis1 - yAxis3;

        final double alphaNumerator = byAxis * cxAxis - bxAxis * cyAxis;
        final double commonDenominator = ayAxis * bxAxis - axAxis * byAxis;
        if (commonDenominator > 0)
        {
            if (alphaNumerator < 0 || alphaNumerator > commonDenominator)
            {
                return false;
            }
        }
        else if (commonDenominator < 0)
        {
            if (alphaNumerator > 0 || alphaNumerator < commonDenominator)
            {
                return false;
            }
        }
        final double betaNumerator = axAxis * cyAxis - ayAxis * cxAxis;
        if (commonDenominator > 0)
        {
            if (betaNumerator < 0 || betaNumerator > commonDenominator)
            {
                return false;
            }
        }
        else if (commonDenominator < 0)
        {
            if (betaNumerator > 0 || betaNumerator < commonDenominator)
            {
                return false;
            }
        }
        if (commonDenominator == 0)
        {
            // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward. The
            // lines are parallel. Check if they're collinear.
            // see "http://mathworld.wolfram.com/Collinear.html"
            final double collinearityTestForP3 = xAxis1 * (yAxis2 - yAxis3)
                    + xAxis2 * (yAxis3 - yAxis1) + xAxis3 * (yAxis1 - yAxis2);
            // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is
            // parallel with p3-p4
            if (collinearityTestForP3 == 0)
            {
                // The lines are collinear. Now check if they overlap.
                if (xAxis1 >= xAxis3 && xAxis1 <= xAxis4 || xAxis1 <= xAxis3 && xAxis1 >= xAxis4
                        || xAxis2 >= xAxis3 && xAxis2 <= xAxis4
                        || xAxis2 <= xAxis3 && xAxis2 >= xAxis4
                        || xAxis3 >= xAxis1 && xAxis3 <= xAxis2
                        || xAxis3 <= xAxis1 && xAxis3 >= xAxis2)
                {
                    if (yAxis1 >= yAxis3 && yAxis1 <= yAxis4 || yAxis1 <= yAxis3 && yAxis1 >= yAxis4
                            || yAxis2 >= yAxis3 && yAxis2 <= yAxis4
                            || yAxis2 <= yAxis3 && yAxis2 >= yAxis4
                            || yAxis3 >= yAxis1 && yAxis3 <= yAxis2
                            || yAxis3 <= yAxis1 && yAxis3 >= yAxis2)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    /**
     * @return True if this segment is exactly east west (the two latitudes are the same)
     */
    public boolean isEastWest()
    {
        return start().hasSameLatitudeAs(end());
    }

    /**
     * @return True if this segment is exactly north south (the two longitudes are the same)
     */
    public boolean isNorthSouth()
    {
        return start().hasSameLongitudeAs(end());
    }

    @Override
    public boolean isPoint()
    {
        return start().equals(end());
    }

    @Override
    public Distance length()
    {
        return this.start().distanceTo(this.end());
    }

    @Override
    public Location middle()
    {
        return new Location(
                Latitude.degrees((this.start().getLatitude().asDegrees()
                        + this.end().getLatitude().asDegrees()) / 2.0),
                Longitude.degrees((this.start().getLongitude().asDegrees()
                        + this.end().getLongitude().asDegrees()) / 2.0));
    }

    @Override
    public Location offsetFromStart(final Ratio ratio)
    {
        final Optional<Heading> heading = heading();
        if (heading.isPresent())
        {
            return this.start().shiftAlongGreatCircle(heading.get(), length().scaleBy(ratio));
        }
        return this.start();
    }

    /**
     * @return The same segment but pointing north if it is not already, by reversing it if it
     *         points south.
     */
    public Segment pointingNorth()
    {
        if (this.isEastWest())
        {
            return this;
        }
        if (start().getLatitude().isLessThan(end().getLatitude()))
        {
            return this;
        }
        return new Segment(end(), start());
    }

    @Override
    public Segment reversed()
    {
        return new Segment(end(), start());
    }

    public Location start()
    {
        return this.first();
    }

    /**
     * The Dot Product of two segments (seen as 2D space vectors)
     *
     * @param that
     *            The other {@link Segment}
     * @return The Dot Product of the two segments
     */
    protected double dotProduct(final Segment that)
    {
        final double thisLatitudeSpan = this.latitudeSpan();
        final double thatLatitudeSpan = that.latitudeSpan();
        final double thisLongitudeSpan = this.longitudeSpan();
        final double thatLongitudeSpan = that.longitudeSpan();
        return thisLatitudeSpan * thatLatitudeSpan + thisLongitudeSpan * thatLongitudeSpan;
    }

    protected double dotProductLength()
    {
        return Math.sqrt(dotProduct(this));
    }

    protected long latitudeSpan()
    {
        return this.end().getLatitude().asDm7() - this.start().getLatitude().asDm7();
    }

    protected long longitudeSpan()
    {
        return this.end().getLongitude().asDm7() - this.start().getLongitude().asDm7();
    }
}
