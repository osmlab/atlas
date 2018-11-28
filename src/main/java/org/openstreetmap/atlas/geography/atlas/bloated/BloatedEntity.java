package org.openstreetmap.atlas.geography.atlas.bloated;

/**
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
