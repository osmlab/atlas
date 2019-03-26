package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
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
 * @author matthieun
 * @author Yazad Khambata
 */
public interface CompleteEntity
{
    static Map<String, String> addNewTag(final Map<String, String> tags, final String key,
            final String value)
    {
        Map<String, String> result = tags;
        if (result == null)
        {
            result = new HashMap<>();
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
        Map<String, String> result = tags;
        if (result == null)
        {
            result = new HashMap<>();
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

    long getIdentifier();

    /**
     * A shallow {@link CompleteEntity} is one that contains only its identifier as effective data.
     *
     * @return if this entity is shallow
     */
    boolean isShallow();

    CompleteEntity withAddedTag(String key, String value);

    CompleteEntity withIdentifier(long identifier);

    CompleteEntity withRelationIdentifiers(Set<Long> relationIdentifiers);

    CompleteEntity withRelations(Set<Relation> relations);

    CompleteEntity withRemovedTag(String key);

    CompleteEntity withReplacedTag(String oldKey, String newKey, String newValue);

    CompleteEntity withTags(Map<String, String> tags);
}
