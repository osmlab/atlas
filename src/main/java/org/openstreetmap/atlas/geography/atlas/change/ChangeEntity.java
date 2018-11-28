package org.openstreetmap.atlas.geography.atlas.change;

import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

/**
 * Utility class for Change entities: ChangeNode, ChangeEdge, etc.
 *
 * @author matthieun
 */
public final class ChangeEntity
{
    static <T extends Object, M extends AtlasEntity> T getAttributeOrBackup(final M source,
            final M override, final Function<M, T> memberExtractor)
    {
        T result = null;
        if (override != null)
        {
            result = memberExtractor.apply(override);
        }
        if (result == null && source != null)
        {
            result = memberExtractor.apply(source);
        }
        if (result == null)
        {
            throw new CoreException("Could not retrieve attribute from source nor backup!");
        }
        return result;
    }

    private ChangeEntity()
    {
    }
}
