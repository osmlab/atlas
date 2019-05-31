package org.openstreetmap.atlas.geography.atlas.items;

import java.util.function.Function;

/**
 * An interface that generalizes enum based declarative connectivity information.
 *
 * @param <ME>
 *            - The current entity.
 * @param <CONNECTED>
 *            - The connected entity.
 * @author Yazad Khambata
 */
public interface ConnectedEntityType<ME extends AtlasEntity, CONNECTED>
{
    String getPropertyName();

    Function<ME, CONNECTED> getAccessFunction();
}
