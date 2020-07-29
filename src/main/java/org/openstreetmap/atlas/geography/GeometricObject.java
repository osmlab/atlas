package org.openstreetmap.atlas.geography;

/**
 * @author matthieun
 */
public interface GeometricObject
{
    boolean intersects(PolyLine other);
}
