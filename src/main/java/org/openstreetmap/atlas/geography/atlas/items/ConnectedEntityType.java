package org.openstreetmap.atlas.geography.atlas.items;

import java.util.function.Function;

/**
 * An interface that generalizes enum based declarative connectivity information.
 *
 * @param <M>
 *            - The current entity.
 * @param <C>
 *            - The connected entity.
 * @author Yazad Khambata
 */
public interface ConnectedEntityType<M extends AtlasEntity, C>
{
    Function<M, C> getAccessFunction();

    String getPropertyName();
}
