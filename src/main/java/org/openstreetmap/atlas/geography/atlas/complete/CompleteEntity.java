package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listenable.TagChangeListenable;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Simple interface for all the Complete entities. As each one extends its parent class already
 * (Node, Edge, Area, ...) this cannot be an abstract class.
 *
 * @param <C>
 *            - the {@link CompleteEntity} implementation.
 * @author matthieun
 * @author Yazad Khambata
 */
public interface CompleteEntity<C extends CompleteEntity<C>> extends TagChangeListenable
{
    static Map<String, String> addNewTag(final Map<String, String> tags, final String key,
            final String value)
    {
        Map<String, String> result = new HashMap<>();
        if (tags != null)
        {
            result = new HashMap<>(tags);
        }
        result.put(key, value);
        return result;
    }

    /**
     * A simple equality check that only looks at identifiers, tags, and parent relations.
     *
     * @param left
     *            the left entity
     * @param right
     *            the right entity
     * @return if the left and right entities are related through a simple equality
     */
    static boolean basicEqual(final AtlasEntity left, final AtlasEntity right)
    {
        return left.getIdentifier() == right.getIdentifier()
                && Objects.equals(left.getTags(), right.getTags())
                && Objects.equals(left.relations(), right.relations());
    }

    static <M, T> boolean equalThroughGet(final M left, final M right, final Function<M, T> getter)
    {
        if (left == null && right == null)
        {
            return true;
        }
        else if (left == null || right == null)
        {
            return false;
        }
        else
        {
            return Objects.equals(getter.apply(left), getter.apply(right));
        }
    }

    /**
     * Create a {@link CompleteEntity} from a given {@link AtlasEntity} reference. The
     * {@link CompleteEntity}'s fields will match the fields of the reference. The returned
     * {@link CompleteEntity} will be full, i.e. all of its associated fields will be non-null.
     *
     * @param reference
     *            the reference to copy
     * @return the full entity
     */
    static AtlasEntity from(final AtlasEntity reference)
    {
        final ItemType type = reference.getType();
        switch (type)
        {
            case NODE:
                return CompleteNode.from((Node) reference);
            case EDGE:
                return CompleteEdge.from((Edge) reference);
            case AREA:
                return CompleteArea.from((Area) reference);
            case LINE:
                return CompleteLine.from((Line) reference);
            case POINT:
                return CompletePoint.from((Point) reference);
            case RELATION:
                return CompleteRelation.from((Relation) reference);
            default:
                throw new CoreException("Unknown ItemType {}", type);
        }
    }

    static Map<String, String> removeTag(final Map<String, String> tags, final String key)
    {
        Map<String, String> result = new HashMap<>();
        if (tags != null)
        {
            result = new HashMap<>(tags);
        }
        result.remove(key);
        return result;
    }

    /**
     * Create a shallow {@link CompleteEntity} from a given {@link AtlasEntity} reference. The
     * {@link CompleteEntity}'s identifier will match the identifier of the reference. The returned
     * {@link CompleteEntity} will be shallow, i.e. all of its associated fields will be null except
     * for the identifier.
     *
     * @param reference
     *            the reference to copy
     * @return the shallow entity
     */
    static AtlasEntity shallowFrom(final AtlasEntity reference)
    {
        final ItemType type = reference.getType();
        switch (type)
        {
            case NODE:
                return CompleteNode.shallowFrom((Node) reference);
            case EDGE:
                return CompleteEdge.shallowFrom((Edge) reference);
            case AREA:
                return CompleteArea.shallowFrom((Area) reference);
            case LINE:
                return CompleteLine.shallowFrom((Line) reference);
            case POINT:
                return CompletePoint.shallowFrom((Point) reference);
            case RELATION:
                return CompleteRelation.shallowFrom((Relation) reference);
            default:
                throw new CoreException("Unknown ItemType {}", type);
        }
    }

    /**
     * Create a shallow {@link CompleteEntity} from a given {@link ItemType} and identifier.
     *
     * @param type
     *            the {@link ItemType}
     * @param identifier
     *            the identifier
     * @return a shallow {@link CompleteEntity} that matches the requested parameters
     */
    static AtlasEntity shallowFrom(final ItemType type, final Long identifier)
    {
        switch (type)
        {
            case NODE:
                return new CompleteNode(identifier);
            case EDGE:
                return new CompleteEdge(identifier);
            case AREA:
                return new CompleteArea(identifier);
            case LINE:
                return new CompleteLine(identifier);
            case POINT:
                return new CompletePoint(identifier);
            case RELATION:
                return new CompleteRelation(identifier);
            default:
                throw new CoreException("Unknown ItemType {}", type);
        }
    }

    static <C extends CompleteEntity<C>> C withAddedTag(final C completeEntity, final String key,
            final String value, final boolean suppressFiringEvent)
    {
        CompleteEntity.withTags(completeEntity,
                CompleteEntity.addNewTag(completeEntity.getTags(), key, value), true);

        if (!suppressFiringEvent)
        {
            completeEntity
                    .fireTagChangeEvent(TagChangeEvent.added(completeEntity.completeItemType(),
                            completeEntity.getIdentifier(), Pair.of(key, value)));
        }

        return completeEntity;
    }

    static <C extends CompleteEntity<C>> C withRemovedTag(final C completeEntity, final String key,
            final boolean suppressFiringEvent)
    {
        CompleteEntity.withTags(completeEntity,
                CompleteEntity.removeTag(completeEntity.getTags(), key), true);

        if (!suppressFiringEvent)
        {
            completeEntity.fireTagChangeEvent(TagChangeEvent.remove(
                    completeEntity.completeItemType(), completeEntity.getIdentifier(), key));
        }

        return completeEntity;
    }

    static <C extends CompleteEntity<C>> C withReplacedTag(final C completeEntity,
            final String oldKey, final String newKey, final String newValue,
            final boolean suppressFiringEvent)
    {
        CompleteEntity.withRemovedTag(completeEntity, oldKey, true);
        CompleteEntity.withAddedTag(completeEntity, newKey, newValue, true);

        if (!suppressFiringEvent)
        {
            completeEntity
                    .fireTagChangeEvent(TagChangeEvent.replaced(completeEntity.completeItemType(),
                            completeEntity.getIdentifier(), Triple.of(oldKey, newKey, newValue)));
        }

        return completeEntity;
    }

    static <C extends CompleteEntity<C>> C withTags(final C completeEntity,
            final Map<String, String> tags, final boolean suppressFiringEvent)
    {
        completeEntity.setTags(tags);

        if (!suppressFiringEvent)
        {
            completeEntity.fireTagChangeEvent(TagChangeEvent.overwrite(
                    completeEntity.completeItemType(), completeEntity.getIdentifier(), tags));
        }

        return completeEntity;
    }

    CompleteItemType completeItemType();

    long getIdentifier();

    Map<String, String> getTags();

    ItemType getType();

    /**
     * A full {@link CompleteEntity} is one one that contains a non-null value for all its fields.
     *
     * @return if this entity is full
     */
    boolean isFull();

    /**
     * A shallow {@link CompleteEntity} is one that contains only its identifier as effective data.
     *
     * @return if this entity is shallow
     */
    boolean isShallow();

    /**
     * Transform this {@link CompleteEntity} into a pretty string. The pretty string for a
     * {@link CompleteEntity} can be customized using different available formats.
     *
     * @param format
     *            the format type for the pretty string
     * @return the pretty string
     */
    String prettify(PrettifyStringFormat format);

    /**
     * Transform this {@link CompleteEntity} into a pretty string. This method uses the default
     * format {@link PrettifyStringFormat#MINIMAL_SINGLE_LINE}.
     *
     * @return the pretty string
     */
    default String prettify()
    {
        return prettify(PrettifyStringFormat.MINIMAL_SINGLE_LINE);
    }

    void setTags(Map<String, String> tags);

    /**
     * Get the WKT for this entity's geometry.
     *
     * @return the WKT of this entity's geometry, null if the geometry is null
     */
    String toWkt();

    default C withAddedTag(final String key, final String value)
    {
        return CompleteEntity.withAddedTag((C) this, key, value, false);
    }

    CompleteEntity withGeometry(Iterable<Location> locations);

    CompleteEntity withIdentifier(long identifier);

    CompleteEntity withRelationIdentifiers(Set<Long> relationIdentifiers);

    CompleteEntity withRelations(Set<Relation> relations);

    default C withRemovedTag(final String key)
    {
        return CompleteEntity.withRemovedTag((C) this, key, false);
    }

    default C withReplacedTag(final String oldKey, final String newKey, final String newValue)
    {
        return CompleteEntity.withReplacedTag((C) this, oldKey, newKey, newValue, false);
    }

    default C withTags(final Map<String, String> tags)
    {
        return CompleteEntity.withTags((C) this, tags, false);
    }
}
