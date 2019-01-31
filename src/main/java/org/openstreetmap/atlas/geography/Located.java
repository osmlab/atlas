package org.openstreetmap.atlas.geography;

/**
 * Contract for any item that can be geographcally bound by a {@link Rectangle}
 *
 * @author matthieun
 */
public interface Located
{
    /**
     * @return The bounds around this located object
     */
    Rectangle bounds();

}
