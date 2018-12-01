package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Utility class for Change entities: ChangeNode, ChangeEdge, etc.
 *
 * @author matthieun
 */
public final class ChangeEntity
{
    /**
     * Filter parent relations that are mentioned in a ChangeEntity to only those that are not null.
     *
     * @param listed
     *            The relation set to filter
     * @param parent
     *            The parent {@link ChangeAtlas}
     * @return the set of {@link ChangeRelation} that are not null.
     */
    static Set<Relation> filterRelations(final Set<Relation> listed, final ChangeAtlas parent)
    {
        return listed.stream().map(relation -> parent.relation(relation.getIdentifier()))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Get either the attribute asked from the change entity (override), or from the backup entity
     * if unavailable.
     * 
     * @param source
     *            The source entity
     * @param override
     *            The change entity (override)
     * @param memberExtractor
     *            Extract the member attribute from that entity
     * @return The corresponding attribute
     */
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
