package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * From a set of {@link PolyLine}s, try to stitch all the {@link PolyLine}s together to form
 * {@link Polygon}s.
 *
 * @author matthieun
 * @author Sid
 */
public class MultiplePolyLineToPolygonsConverter
        implements Converter<Iterable<PolyLine>, Iterable<Polygon>>
{
    /**
     * @author Sid
     */
    public static class OpenPolygonException extends CoreException
    {
        private static final long serialVersionUID = -278028096455310936L;

        private final List<Location> openLocations;

        public OpenPolygonException(final String message, final List<Location> openLocations)
        {
            super(message + " Open Locations are: " + openLocations.toString());
            this.openLocations = openLocations;
        }

        public OpenPolygonException(final String message, final List<Location> openLocations,
                final Object... arguments)
        {
            super(message + " Open Locations are: " + openLocations.toString(), arguments);
            this.openLocations = openLocations;
        }

        public OpenPolygonException(final String message, final List<Location> openLocations,
                final Throwable cause, final Object... arguments)
        {
            super(message + " Open Locations are: " + openLocations.toString(), cause, arguments);
            this.openLocations = openLocations;
        }

        public List<Location> getOpenLocations()
        {
            return this.openLocations;
        }
    }

    /**
     * Simple object containing connectivity information about two {@link PolyLine}s. "connected"
     * set to "true" means the two {@link PolyLine}s connect at some end. "reversed" set to true
     * means that one of the two {@link PolyLine}s had to be reversed to connect.
     *
     * @author matthieun
     */
    private static class ConnectResult
    {
        private final boolean connected;
        private final boolean reversed;

        ConnectResult(final boolean connected, final boolean reversed)
        {
            this.connected = connected;
            this.reversed = reversed;
        }

        public boolean isConnected()
        {
            return this.connected;
        }

        public boolean isReversed()
        {
            return this.reversed;
        }

        @Override
        public String toString()
        {
            return this.connected ? "Connected" + (this.reversed ? " and reversed" : "")
                    : "Not connected";
        }
    }

    /**
     * A {@link Polygon} in construction, with many other {@link PolyLine}s
     *
     * @author matthieun
     */
    private static class PossiblePolygon
    {
        private boolean completed;
        // An ordered list of polylines, based on connectivity
        private final List<PolyLine> polyLines = new ArrayList<>();

        PossiblePolygon(final PolyLine first)
        {
            this.completed = first instanceof Polygon || first.first().equals(first.last());
            this.polyLines.add(first);
        }

        /**
         * @param candidate
         *            A polyLine to attach to that possible polygon
         * @return True if the polyLine was successfully attached.
         */
        public boolean attach(final PolyLine candidate)
        {
            boolean result = false;
            final ConnectResult canAppendCandidateToLine = canAppendSecondToFirst(lastPolyLine(),
                    candidate);
            final ConnectResult canPrependCandidateToLine = canPrependFirstToSecond(candidate,
                    firstPolyLine());
            PolyLine toAdd = candidate;
            if (canAppendCandidateToLine.isConnected())
            {
                if (canAppendCandidateToLine.isReversed())
                {
                    toAdd = toAdd.reversed();
                }
                if (toAdd.size() > 1)
                {
                    toAdd = trimFirst(toAdd);
                }
                else
                {
                    if (canPrependCandidateToLine.isConnected())
                    {
                        this.completed = true;
                    }
                    return true;
                }
            }
            if (canPrependCandidateToLine.isConnected())
            {
                if (canPrependCandidateToLine.isReversed())
                {
                    if (canAppendCandidateToLine.isConnected())
                    {
                        // Already reversed previously
                    }
                    else
                    {
                        toAdd = toAdd.reversed();
                    }
                }
                if (toAdd.size() > 1)
                {
                    toAdd = trimLast(toAdd);
                }
                else
                {
                    if (canAppendCandidateToLine.isConnected())
                    {
                        this.completed = true;
                    }
                    return true;
                }
            }
            if (canAppendCandidateToLine.isConnected())
            {
                this.polyLines.add(toAdd);
                result = true;
            }
            else if (canPrependCandidateToLine.isConnected())
            {
                this.polyLines.add(0, toAdd);
                result = true;
            }
            if (canPrependCandidateToLine.isConnected() && canAppendCandidateToLine.isConnected())
            {
                this.completed = true;
            }
            return result;
        }

        public Location firstLocation()
        {
            return this.polyLines.get(0).first();
        }

        public boolean isCompleted()
        {
            return this.completed;
        }

        public Location lastLocation()
        {
            return this.polyLines.get(this.polyLines.size() - 1).last();
        }

        public int size()
        {
            return this.polyLines.size();
        }

        public Polygon toPolygon()
        {
            if (!this.isCompleted() && this.size() >= 1)
            {
                // If that method is called and the PossiblePolygon is not closed (i.e. completed)
                // we gather the first and end point of the partially completed polyline and throw
                // an exception.
                final List<Location> openLocations = new ArrayList<>();
                final Location firstLocation = this.polyLines.get(0).first();
                final Location lastLocation = this.polyLines.get(this.size() - 1).last();
                if (firstLocation != null && lastLocation != null)
                {
                    openLocations.add(firstLocation);
                    openLocations.add(lastLocation);
                    throw new OpenPolygonException(
                            "Cannot build polygon with multiple polylines. Loop is not closed.",
                            openLocations);
                }
            }
            return new Polygon(new MultiIterable<>(this.polyLines));
        }

        @Override
        public String toString()
        {
            final StringList list = new StringList();
            this.polyLines.forEach(polyLine -> list.add(polyLine.first() + " -> "));
            list.add(this.lastLocation());
            return list.join("");
        }

        /**
         * Test if two {@link PolyLine}s connect by appending the second {@link PolyLine} (straight
         * or reversed) to the first one (unchanged).
         *
         * @param one
         *            The {@link PolyLine} from which the end will be considered
         * @param two
         *            The {@link PolyLine} from which the start will be considered
         * @return ConnectResult: connected = true if the end of one is the same as the start of two
         *         and reversed = true if the {@link PolyLine} two had to be reversed to be able to
         *         connect the end of one to the beginning of two.
         */
        private ConnectResult canAppendSecondToFirst(final PolyLine one, final PolyLine two)
        {
            if (one.last().equals(two.first()))
            {
                return new ConnectResult(true, false);
            }
            else if (one.last().equals(two.last()))
            {
                return new ConnectResult(true, true);
            }
            else
            {
                return new ConnectResult(false, false);
            }
        }

        /**
         * Test if two {@link PolyLine}s connect by prepending the first {@link PolyLine} (straight
         * or reversed) to the second one (unchanged).
         *
         * @param one
         *            The {@link PolyLine} from which the end will be considered
         * @param two
         *            The {@link PolyLine} from which the start will be considered
         * @return ConnectResult: connected = true if the end of one is the same as the start of two
         *         and reversed = true if the {@link PolyLine} one had to be reversed to be able to
         *         connect the end of one to the beginning of two.
         */
        private ConnectResult canPrependFirstToSecond(final PolyLine one, final PolyLine two)
        {
            if (one.last().equals(two.first()))
            {
                return new ConnectResult(true, false);
            }
            else if (one.first().equals(two.first()))
            {
                return new ConnectResult(true, true);
            }
            else
            {
                return new ConnectResult(false, false);
            }
        }

        private PolyLine firstPolyLine()
        {
            return this.polyLines.get(0);
        }

        private PolyLine lastPolyLine()
        {
            return this.polyLines.get(this.polyLines.size() - 1);
        }

        /**
         * Remove the first point of this {@link PolyLine} to append it to another {@link PolyLine}
         *
         * @param current
         *            The {@link PolyLine} to trim
         * @return The {@link PolyLine} trimmed of its first point.
         */
        private PolyLine trimFirst(final PolyLine current)
        {
            final List<Location> result = new ArrayList<>();
            for (final Location location : current)
            {
                result.add(location);
            }
            result.remove(0);
            return new PolyLine(result);
        }

        /**
         * Remove the last point of this {@link PolyLine} to prepend it to another {@link PolyLine}
         *
         * @param current
         *            The {@link PolyLine} to trim
         * @return The {@link PolyLine} trimmed of its last point.
         */
        private PolyLine trimLast(final PolyLine current)
        {
            final List<Location> result = new ArrayList<>();
            for (final Location location : current)
            {
                result.add(location);
            }
            result.remove(result.size() - 1);
            return new PolyLine(result);
        }
    }

    @Override
    public Iterable<Polygon> convert(final Iterable<PolyLine> candidates)
    {
        // The complete polygons
        final List<PossiblePolygon> completes = new ArrayList<>();
        // The polygons that have been started, but that are incomplete.
        final List<PossiblePolygon> incompletes = new ArrayList<>();
        // The polyLines that have not found a match yet
        final LinkedList<PolyLine> remainingPolyLines = new LinkedList<>();
        candidates.forEach(remainingPolyLines::add);
        int iterationsSinceLastPolyLineTaken = 0;
        while (!remainingPolyLines.isEmpty()
                && iterationsSinceLastPolyLineTaken <= remainingPolyLines.size())
        {
            final PolyLine candidate = remainingPolyLines.removeFirst();
            boolean added = false;
            if (!incompletes.isEmpty())
            {
                // There are some incompletes. Always try to fill the incompletes to the end until
                // they are complete before creating new incomplete polygons.
                boolean completed = false;
                int index = -1;
                // Try the candidate polyline with all the incomplete polygons
                for (final PossiblePolygon incomplete : incompletes)
                {
                    index++;
                    if (incomplete.attach(candidate))
                    {
                        added = true;
                        completed = incomplete.isCompleted();
                        break;
                    }
                }
                if (completed)
                {
                    final PossiblePolygon increased = incompletes.get(index);
                    incompletes.remove(index);
                    completes.add(increased);
                }
            }
            else
            {
                // There are no incomplete polygons, just create one.
                final PossiblePolygon incompleteCandidate = new PossiblePolygon(candidate);
                if (incompleteCandidate.isCompleted())
                {
                    completes.add(incompleteCandidate);
                }
                else
                {
                    incompletes.add(incompleteCandidate);
                }
                added = true;
            }

            if (!added)
            {
                // Could not add the polyline to any incomplete polygon, so adding it back to the
                // end of the list. It might get better luck once those incomplete polygons have
                // grown a bit more.
                remainingPolyLines.addLast(candidate);
                iterationsSinceLastPolyLineTaken++;
            }
            else
            {
                iterationsSinceLastPolyLineTaken = 0;
            }
        }
        if (!incompletes.isEmpty())
        {
            throw new OpenPolygonException("Unable to close all the polygons!",
                    Iterables
                            .stream(incompletes).flatMap(incomplete -> Iterables
                                    .from(incomplete.firstLocation(), incomplete.lastLocation()))
                            .collectToList());
        }
        return completes.stream().map(PossiblePolygon::toPolygon).collect(Collectors.toList());
    }
}
