package org.openstreetmap.atlas.geography;

/**
 * Contract for any item that can be geographcally bound by a {@link Rectangle}
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public interface Located
{
    /**
     * @return The bounds around this located object
     */
    Rectangle bounds();

    /**
     * Return {@code true} if surface fully geometrically encloses {@code this}.
     * <p>
     * For backward compatibility a default implementation that fails is added.
     *
     * @param surface
     *            - check if {@code this} is fully within the surface.
     * @return - {@code true} if fully within surface, false otherwise.
     */
    default boolean within(GeometricSurface surface)
    {
        throw new UnsupportedOperationException();
    }
}
