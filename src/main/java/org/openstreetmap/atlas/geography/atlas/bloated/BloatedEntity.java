package org.openstreetmap.atlas.geography.atlas.bloated;

/**
 * Simple interface for all the Bloated entities. As each one extends its parent class already
 * (Node, Edge, Area, ...) this cannot be an abstract class.
 *
 * @author matthieun
 */
public interface BloatedEntity
{
    long getIdentifier();

    /**
     * @return True when that entity contains only its identifier as effective data.
     */
    boolean isSuperShallow();
}
