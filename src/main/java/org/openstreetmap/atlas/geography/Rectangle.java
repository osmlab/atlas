package org.openstreetmap.atlas.geography;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;

import org.opengis.geometry.BoundingBox;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A rectangle on the surface of earth. It cannot span the date change line (longitude -180)
 *
 * @author matthieun
 */
public final class Rectangle extends Polygon
{
    private static final long serialVersionUID = 6940095569975683891L;

    public static final Rectangle MAXIMUM = forCorners(
            new Location(Latitude.MINIMUM, Longitude.MINIMUM),
            new Location(Latitude.MAXIMUM, Longitude.MAXIMUM));
    public static final Rectangle MINIMUM = forCorners(Location.CENTER, Location.CENTER);
    public static final Rectangle TEST_RECTANGLE = forString(
            "37.328167,-122.031905:37.330394,-122.029051");
    public static final Rectangle TEST_RECTANGLE_2 = forString(
            "37.325194,-122.034281:37.325683,-122.033500");

    // A rectangle stores only two locations, despite being a 4 location Polygon.
    private final Location lowerLeft;
    private final Location upperRight;

    public static Rectangle forBoundingBox(final BoundingBox boundingBox)
    {
        return Rectangle.forLocations(
                new Location(Latitude.degrees(boundingBox.getMinY()),
                        Longitude.degrees(boundingBox.getMinX())),
                new Location(Latitude.degrees(boundingBox.getMaxY()),
                        Longitude.degrees(boundingBox.getMaxX())));
    }

    /**
     * Create a {@link Rectangle} from the lower left and upper right corners
     *
     * @param lowerLeft
     *            The lower left corner
     * @param upperRight
     *            The upper right corner
     * @return The resulting {@link Rectangle}
     */
    public static Rectangle forCorners(final Location lowerLeft, final Location upperRight)
    {
        if (lowerLeft == null || upperRight == null)
        {
            throw new CoreException("Cannot build a Rectangle with one of the corners being null.");
        }
        return new Rectangle(lowerLeft, upperRight);
    }

    /**
     * Build a {@link Rectangle} that wraps around an {@link Iterable} of {@link Located} objects.
     *
     * @param locateds
     *            The {@link Iterable} of {@link Located} objects
     * @param <T>
     *            The type of located object.
     * @return The resulting {@link Rectangle}
     */
    public static <T extends Located> Rectangle forLocated(final Iterable<T> locateds)
    {
        Latitude lower = null;
        Latitude upper = null;
        Longitude left = null;
        Longitude right = null;
        boolean hasOneItem = false;
        for (final Located located : locateds)
        {
            hasOneItem = true;
            for (final Location location : located.bounds())
            {
                final Latitude latitude = location.getLatitude();
                final Longitude longitude = location.getLongitude();
                if (lower == null || latitude.isLessThan(lower))
                {
                    lower = latitude;
                }
                if (upper == null || latitude.isGreaterThan(upper))
                {
                    upper = latitude;
                }
                if (left == null || longitude.isLessThan(left))
                {
                    left = longitude;
                }
                if (right == null || longitude.isGreaterThan(right))
                {
                    right = longitude;
                }
            }
        }
        if (!hasOneItem)
        {
            throw new CoreException(
                    "Rectangle.forLocated(Iterable<Located>) has to have at least one item in the Iterable<Located>");
        }
        return forCorners(new Location(lower, left), new Location(upper, right));
    }

    /**
     * Build a {@link Rectangle} that wraps around an array of {@link Located} objects.
     *
     * @param locateds
     *            The array of {@link Located} objects
     * @return The resulting {@link Rectangle}
     */
    public static Rectangle forLocated(final Located... locateds)
    {
        return forLocated(Iterables.iterable(locateds));
    }

    /**
     * Build a {@link Rectangle} that wraps around an {@link Iterable} of {@link Location} objects.
     *
     * @param locations
     *            The {@link Iterable} of {@link Location} objects
     * @return The resulting {@link Rectangle}
     */
    public static Rectangle forLocations(final Iterable<Location> locations)
    {
        Latitude lower = null;
        Latitude upper = null;
        Longitude left = null;
        Longitude right = null;
        for (final Location location : locations)
        {
            final Latitude latitude = location.getLatitude();
            final Longitude longitude = location.getLongitude();
            if (lower == null || latitude.isLessThan(lower))
            {
                lower = latitude;
            }
            if (upper == null || latitude.isGreaterThan(upper))
            {
                upper = latitude;
            }
            if (left == null || longitude.isLessThan(left))
            {
                left = longitude;
            }
            if (right == null || longitude.isGreaterThan(right))
            {
                right = longitude;
            }
        }
        return forCorners(new Location(lower, left), new Location(upper, right));
    }

    /**
     * Build a {@link Rectangle} that wraps around an array of {@link Location} objects.
     *
     * @param locations
     *            The array of {@link Location} objects
     * @return The resulting {@link Rectangle}
     */
    public static Rectangle forLocations(final Location... locations)
    {
        return Rectangle.forLocations(Iterables.iterable(locations));
    }

    /**
     * @param rectangleString
     *            The string definition
     * @return The resulting {@link Rectangle} parsed from its string definition
     */
    public static Rectangle forString(final String rectangleString)
    {
        final StringList split = StringList.split(rectangleString, ":");
        if (split.size() != 2)
        {
            throw new CoreException("Invalid Rectangle String: {}", rectangleString);
        }
        return forLocations(Location.forString(split.get(0)), Location.forString(split.get(1)));
    }

    /**
     * Private constructor using the two corners
     *
     * @param lowerLeft
     *            The lower left corner
     * @param upperRight
     *            The upper right corner
     */
    private Rectangle(final Location lowerLeft, final Location upperRight)
    {
        super(lowerLeft, new Location(upperRight.getLatitude(), lowerLeft.getLongitude()),
                upperRight, new Location(lowerLeft.getLatitude(), upperRight.getLongitude()));
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    /**
     * @return JTS object {@link Envelope}, which is an equivalent of {@link Rectangle}
     */
    public Envelope asEnvelope()
    {
        return new Envelope(this.lowerLeft.getLongitude().asDegrees(),
                this.upperRight.getLongitude().asDegrees(),
                this.lowerLeft.getLatitude().asDegrees(),
                this.upperRight.getLatitude().asDegrees());
    }

    @Override
    public Rectangle bounds()
    {
        return this;
    }

    @Override
    public Location center()
    {
        return new Segment(this.lowerLeft, this.upperRight).middle();
    }

    /**
     * Contract the rectangle in 4 directions as far as possible. If the distance to move the
     * corners would invert the rectangle then the side(s) will collapse into length 0. The most
     * that it can contract is to a single point in the middle.
     *
     * @param distance
     *            to contract the four corners
     * @return new rectangle with contracted dimensions
     */
    public Rectangle contract(final Distance distance)
    {
        final Location newLowerLeft = this.lowerLeft.shiftAlongGreatCircle(Heading.NORTH, distance)
                .shiftAlongGreatCircle(Heading.EAST, distance);
        final Location newUpperRight = this.upperRight
                .shiftAlongGreatCircle(Heading.SOUTH, distance)
                .shiftAlongGreatCircle(Heading.WEST, distance);
        final boolean tooShortHeight = newLowerLeft.getLatitude()
                .isGreaterThan(newUpperRight.getLatitude());
        final boolean tooShortWidth = newLowerLeft.getLongitude()
                .isGreaterThan(newUpperRight.getLongitude());
        if (tooShortHeight && tooShortWidth)
        {
            return this.center().bounds();
        }
        else
        {
            final Location lowerRight = new Location(this.lowerLeft().getLatitude(),
                    this.upperRight().getLongitude());
            if (tooShortHeight)
            {
                final Latitude sharedLatitude = lowerRight.midPoint(this.upperRight())
                        .getLatitude();
                return forCorners(new Location(sharedLatitude, newLowerLeft.getLongitude()),
                        new Location(sharedLatitude, newUpperRight.getLongitude()));
            }
            else if (tooShortWidth)
            {
                final Longitude sharedLongitude = lowerRight.midPoint(this.lowerLeft())
                        .getLongitude();
                return forCorners(new Location(newLowerLeft.getLatitude(), sharedLongitude),
                        new Location(newUpperRight.getLatitude(), sharedLongitude));
            }
            else
            {
                return forCorners(newLowerLeft, newUpperRight);
            }
        }
    }

    /**
     * @param that
     *            The other {@link Rectangle} to combine
     * @return The {@link Rectangle} wrapping this {@link Rectangle} and the one passed as an
     *         argument.
     */
    public Rectangle combine(final Rectangle that)
    {
        return Rectangle.forLocations(this.lowerLeft, this.upperRight, that.lowerLeft,
                that.upperRight);
    }

    /**
     * Expand a given distance on four directions
     *
     * @param distance
     *            The {@link Distance} to expand
     * @return The expanded {@link Rectangle}
     */
    public Rectangle expand(final Distance distance)
    {
        final Location newLowerLeft = this.lowerLeft.shiftAlongGreatCircle(Heading.SOUTH, distance)
                .shiftAlongGreatCircle(Heading.WEST, distance);
        final Location newUpperRight = this.upperRight
                .shiftAlongGreatCircle(Heading.NORTH, distance)
                .shiftAlongGreatCircle(Heading.EAST, distance);
        return forCorners(newLowerLeft, newUpperRight);
    }

    /**
     * Expand a given distance horizontally, on both directions
     *
     * @param distance
     *            The {@link Distance} to expand
     * @return The expanded {@link Rectangle}
     */
    public Rectangle expandHorizontally(final Distance distance)
    {
        final Location newLowerLeft = this.lowerLeft.shiftAlongGreatCircle(Heading.WEST, distance);
        final Location newUpperRight = this.upperRight.shiftAlongGreatCircle(Heading.EAST,
                distance);
        return forCorners(newLowerLeft, newUpperRight);
    }

    /**
     * Expand a given distance vertically, on both directions
     *
     * @param distance
     *            The {@link Distance} to expand
     * @return The expanded {@link Rectangle}
     */
    public Rectangle expandVertically(final Distance distance)
    {
        final Location newLowerLeft = this.lowerLeft.shiftAlongGreatCircle(Heading.SOUTH, distance);
        final Location newUpperRight = this.upperRight.shiftAlongGreatCircle(Heading.NORTH,
                distance);
        return forCorners(newLowerLeft, newUpperRight);
    }

    /**
     * Test if this rectangle fully encloses a {@link Located} item
     *
     * @param item
     *            The item to test
     * @return True if this rectangle contains a {@link Located} item
     */
    public boolean fullyGeometricallyEncloses(final Located item)
    {
        final Rectangle bounds = item instanceof Rectangle ? (Rectangle) item : item.bounds();
        return this.lowerLeft().getLatitude().isLessThanOrEqualTo(bounds.lowerLeft().getLatitude())
                && this.lowerLeft().getLongitude()
                        .isLessThanOrEqualTo(bounds.lowerLeft().getLongitude())
                && this.upperRight().getLatitude()
                        .isGreaterThanOrEqualTo(bounds.upperRight().getLatitude())
                && this.upperRight().getLongitude()
                        .isGreaterThanOrEqualTo(bounds.upperRight().getLongitude());
    }

    @Override
    public boolean fullyGeometricallyEncloses(final Location item)
    {
        return this.fullyGeometricallyEncloses((Located) item);
    }

    @Override
    public boolean fullyGeometricallyEncloses(final Rectangle item)
    {
        return this.fullyGeometricallyEncloses((Located) item);
    }

    /**
     * @return The height of this {@link Rectangle}
     */
    public Angle height()
    {
        return Angle
                .dm7(this.upperRight.getLatitude().asDm7() - this.lowerLeft.getLatitude().asDm7());
    }

    /**
     * @param other
     *            The other {@link Rectangle} to intersect
     * @return The intersection of the two rectangles
     */
    public Rectangle intersection(final Rectangle other)
    {
        if (other == null)
        {
            return null;
        }
        if (this.equals(other))
        {
            return this;
        }
        if (this.fullyGeometricallyEncloses(other))
        {
            return other;
        }
        if (other.fullyGeometricallyEncloses(this))
        {
            return this;
        }
        final Set<Location> intersections = this.intersections(other);
        if (intersections.size() == 0)
        {
            return null;
        }
        if (intersections.size() == 1)
        {
            return Rectangle.forLocations(intersections.iterator().next());
        }
        if (intersections.size() == 2)
        {
            final Iterator<Location> iterator = intersections.iterator();
            final Location location1 = iterator.next();
            final Location location2 = iterator.next();
            if (!location1.getLatitude().equals(location2.getLatitude())
                    && !location1.getLongitude().equals(location2.getLongitude()))
            {
                return Rectangle.forLocations(location1, location2);
            }
            else
            {
                if (location1.getLatitude().equals(location2.getLatitude()))
                {
                    if (this.width().isLessThanOrEqualTo(other.width()))
                    {
                        for (final Location missing : this)
                        {
                            if (other.fullyGeometricallyEncloses(missing))
                            {
                                return Rectangle.forLocations(location1, location2, missing);
                            }
                        }
                    }
                    else
                    {
                        for (final Location missing : other)
                        {
                            if (this.fullyGeometricallyEncloses(missing))
                            {
                                return Rectangle.forLocations(location1, location2, missing);
                            }
                        }
                    }
                }
                if (location1.getLongitude().equals(location2.getLongitude()))
                {
                    if (this.height().isLessThanOrEqualTo(other.height()))
                    {
                        for (final Location missing : this)
                        {
                            if (other.fullyGeometricallyEncloses(missing))
                            {
                                return Rectangle.forLocations(location1, location2, missing);
                            }
                        }
                    }
                    else
                    {
                        for (final Location missing : other)
                        {
                            if (this.fullyGeometricallyEncloses(missing))
                            {
                                return Rectangle.forLocations(location1, location2, missing);
                            }
                        }
                    }
                }
            }
        }
        throw new CoreException("Cannot have more than 2 intersections.");
    }

    /**
     * @return The lower left corner {@link Location} of this {@link Rectangle}
     */
    public Location lowerLeft()
    {
        return this.lowerLeft;
    }

    /**
     * @return The lower right corner {@link Location} of this {@link Rectangle}
     */
    public Location lowerRight()
    {
        return new Location(this.lowerLeft.getLatitude(), this.upperRight.getLongitude());
    }

    @Override
    public boolean overlaps(final PolyLine other)
    {
        if (other instanceof Rectangle)
        {
            final Rectangle otherRectangle = (Rectangle) other;
            return !(otherRectangle.lowerLeft.getLongitude()
                    .isGreaterThan(this.upperRight.getLongitude())
                    || otherRectangle.upperRight.getLongitude()
                            .isLessThan(this.lowerLeft.getLongitude())
                    || otherRectangle.upperRight.getLatitude()
                            .isLessThan(this.lowerLeft.getLatitude())
                    || otherRectangle.lowerLeft.getLatitude()
                            .isGreaterThan(this.upperRight.getLatitude()));
        }
        else
        {
            return super.overlaps(other);
        }
    }

    @Override
    public Surface surface()
    {
        return Surface.forAngles(height(), width());
    }

    @Override
    public String toCompactString()
    {
        return this.lowerLeft.toCompactString() + ":" + this.upperRight.toCompactString();
    }

    /**
     * @return The upper left corner {@link Location} of this {@link Rectangle}
     */
    public Location upperLeft()
    {
        return new Location(this.upperRight.getLatitude(), this.lowerLeft.getLongitude());
    }

    /**
     * @return The upper right corner {@link Location} of this {@link Rectangle}
     */
    public Location upperRight()
    {
        return this.upperRight;
    }

    /**
     * @return The width of this {@link Rectangle}
     */
    public Angle width()
    {
        return Angle.dm7(
                this.upperRight.getLongitude().asDm7() - this.lowerLeft.getLongitude().asDm7());
    }

    protected Rectangle2D asAwtRectangle()
    {
        final int xAxis = (int) this.upperLeft().getLongitude().asDm7();
        final int yAxis = (int) this.upperLeft().getLatitude().asDm7();
        final int width = (int) (this.upperRight().getLongitude().asDm7()
                - this.upperLeft().getLongitude().asDm7());
        final int height = (int) (this.upperLeft().getLatitude().asDm7()
                - this.lowerLeft().getLatitude().asDm7());
        return new java.awt.Rectangle(xAxis, yAxis, width, height);
    }
}
