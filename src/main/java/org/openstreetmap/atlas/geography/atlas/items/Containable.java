package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.GeometricSurface;

/**
 * Represents entities that can be contained inside a {@link GeometricSurface}.
 *
 * @author Yazad Khambata
 */
public interface Containable
{

    /**
     * Return {@code true} if surface fully geometrically encloses {@code this}.
     *
     * @param surface
     *            - check if {@code this} is fully within the surface.
     * @return - {@code true} if fully within surface, false otherwise.
     */
    boolean within(GeometricSurface surface);
}
