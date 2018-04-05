package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.utilities.scalars.Surface;

/**
 * An interface for all geometric surface objects
 *
 * @author jklamer
 */
public interface GeometricSurface extends Located
{
    boolean fullyGeometricallyEncloses(Location location);

    boolean fullyGeometricallyEncloses(PolyLine polyLine);

    boolean fullyGeometricallyEncloses(MultiPolygon multiPolygon);

    boolean overlaps(PolyLine polyLine);

    boolean overlaps(MultiPolygon multiPolygon);

    /**
     * @return The {@link Surface} of this {@link GeometricSurface}. Not valid if the
     *         {@link GeometricSurface} self-intersects, and/or overlaps itself
     * @see "http://www.mathopenref.com/coordpolygonarea2.html"
     */
    Surface surface();

    /**
     * @return The approximate {@link Surface} of this {@link GeometricSurface} if it were projected
     *         onto the Earth. Not valid if the {@link GeometricSurface} self-intersects, and/or
     *         overlaps itself. Uses "Some Algorithms for Polygons on a Sphere" paper as reference.
     * @see "https://trs.jpl.nasa.gov/bitstream/handle/2014/41271/07-0286.pdf"
     */
    Surface surfaceOnSphere();
}
