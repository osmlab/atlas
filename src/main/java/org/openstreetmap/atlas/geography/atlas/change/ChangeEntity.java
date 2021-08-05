package org.openstreetmap.atlas.geography.atlas.change;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
     * Get either the attribute asked from the source entity
     *
     * @param name
     *            The name of the extraction operation
     * @param source
     *            The source entity
     * @param memberExtractor
     *            Extract the member attribute from that entity
     * @return The corresponding attribute
     */
    static <T extends Object, M extends AtlasEntity> T getAttribute(final M source,
            final Function<M, T> memberExtractor, final String name)
    {
        T result = null;
        if (result == null && source != null)
        {
            result = memberExtractor.apply(source);
        }
        return result;
    }

    /**
     * Get all the available attributes asked from the change entity (override), and/or from the
     * backup entity.
     *
     * @param name
     *            The name of the extraction operation
     * @param source
     *            The source entity
     * @param override
     *            The change entity (override)
     * @param memberExtractor
     *            Extract the member attribute from that entity
     * @return The corresponding attribute list. Will not be empty.
     */
    static <T extends Object, M extends AtlasEntity> List<T> getAttributeAndOptionallyBackup(
            final M source, final M override, final Function<M, T> memberExtractor,
            final String name)
    {
        final List<T> result = new ArrayList<>();
        if (override != null)
        {
            final T member = memberExtractor.apply(override);
            if (member != null)
            {
                result.add(member);
            }

        }
        if (source != null)
        {
            final T member = memberExtractor.apply(source);
            if (member != null)
            {
                result.add(member);
            }
        }
        if (result.isEmpty())
        {
            throw new CoreException(
                    "Could not retrieve attribute \"{}\" from override nor source!\noverride: {}\nsource:{}",
                    name, override, source);
        }
        return result;
    }

    /**
     * Get either the attribute asked from the change entity (override), or from the backup entity
     * if unavailable.
     *
     * @param name
     *            The name of the extraction operation
     * @param source
     *            The source entity
     * @param override
     *            The change entity (override)
     * @param memberExtractor
     *            Extract the member attribute from that entity
     * @return The corresponding attribute
     */
    static <T extends Object, M extends AtlasEntity> T getAttributeOrBackup(final M source,
            final M override, final Function<M, T> memberExtractor, final String name)
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
            throw new CoreException(
                    "Could not retrieve attribute \"{}\" from override nor source!\noverride: {}\nsource:{}",
                    name, override, source);
        }
        return result;
    }

    /**
     * @param <V>
     *            The cached value type
     * @param fieldCache
     *            The cache
     * @param cacheSetter
     *            A function that will set the cache not null in case it was null.
     * @param lock
     *            The synchronization lock to access the cache
     * @param creator
     *            The original creator of the type if the cache does not contain it.
     * @return Either the cached value or the freshly created one.
     */
    static <V> V getOrCreateCache(final V fieldCache, final Consumer<V> cacheSetter,
            final Object lock, final Supplier<V> creator)
    {
        V localRelationCache = fieldCache;
        if (localRelationCache == null)
        {
            synchronized (lock) // NOSONAR
            {
                localRelationCache = fieldCache; // NOSONAR
                if (localRelationCache == null) // NOSONAR
                {
                    localRelationCache = creator.get();
                    cacheSetter.accept(localRelationCache);
                }
            }
        }
        return localRelationCache;
    }

    private ChangeEntity()
    {
    }
}
