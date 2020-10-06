package org.openstreetmap.atlas.geography;

/**
 * @author matthieun
 */
public interface GeometricObject
{
    double SIMILARITY_THRESHOLD = 0.9999999;

    boolean intersects(PolyLine other);
}
