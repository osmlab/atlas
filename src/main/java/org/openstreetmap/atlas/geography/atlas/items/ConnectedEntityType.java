package org.openstreetmap.atlas.geography.atlas.items;

import java.util.function.Function;

/**
 * @author Yazad Khambata
 */
public interface ConnectedEntityType<ME extends AtlasEntity, RELATED>
{
    String getPropertyName();

    Function<ME, RELATED> getAccessFunction();
}
