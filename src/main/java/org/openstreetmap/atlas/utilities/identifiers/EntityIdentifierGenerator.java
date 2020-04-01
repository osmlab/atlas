package org.openstreetmap.atlas.utilities.identifiers;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * Generate unique 64 bit (Java long) identifiers for {@link CompleteEntity}s. The identifiers are
 * generated using a hash of the entity's properties, including its geometry and tags. While the
 * identifiers are advertised as unique, 64 bits may not be enough to prevent collisions when used
 * at world-scale.
 * 
 * @author lcram
 */
public class EntityIdentifierGenerator
{
    private static final long HIGHEST_ATLAS_ID = 900000000999999L;
    private static final long LOWEST_ATLAS_ID = -900000000999999L;

    /**
     * Generate a 64 bit hash for a given non-{@link Edge} {@link CompleteEntity}. The entity must
     * contain enough information for it to be created from scratch.
     * 
     * @param entity
     *            the entity
     * @return the hash
     */
    public long generateIdentifier(final CompleteEntity<?> entity)
    {
        if (entity instanceof CompleteEdge)
        {
            throw new IllegalArgumentException(
                    "For CompleteEdge, please use generatePositiveIdentifierForEdge");
        }
        return generate(entity, true);
    }

    /**
     * Generate a 64 bit hash for a given {@link CompleteEdge}. The edge must contain enough
     * information for it to be created from scratch. The ID generated from this method will always
     * be positive.
     *
     * @param edge
     *            the edge
     * @return the hash
     */
    public long generatePositiveIdentifierForEdge(final CompleteEdge edge)
    {
        return generate(edge, false);
    }

    /**
     * Given some {@link CompleteEntity}, compute a string made up of the concatenated basic entity
     * properties (i.e. the geometry WKT and the tags).
     *
     * @param entity
     *            the {@link CompleteEntity} to string-ify
     * @return the property string
     */
    String getBasicPropertyString(final CompleteEntity<?> entity)
    {
        final String wkt = entity.toWkt();
        if (wkt == null && !(entity instanceof CompleteRelation))
        {
            throw new CoreException("Geometry must be set for entity {}", entity.prettify());
        }

        final Map<String, String> tags = entity.getTags();
        if (tags == null)
        {
            throw new CoreException("Tags must be set for entity {}", entity.prettify());
        }
        final SortedSet<String> sortedTags = tags.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toCollection(TreeSet::new));
        final String tagString = new StringList(sortedTags).join(",");

        return wkt + ";" + tagString;
    }

    /**
     * Given some {@link CompleteEntity}, compute a string made up of concatenated type specific
     * entity properties (e.g. for a {@link CompleteNode} this would be the in/out {@link Edge}
     * identifiers).
     *
     * @param entity
     *            the {@link CompleteEntity} to string-ify
     * @return the property string
     */
    String getTypeSpecificPropertyString(final CompleteEntity<?> entity)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(";");
        switch (entity.getType())
        {
            case EDGE:
                final CompleteEdge edge = (CompleteEdge) entity;
                if (edge.start() != null)
                {
                    builder.append(edge.start().getIdentifier());
                }
                builder.append(";");
                if (edge.end() != null)
                {
                    builder.append(edge.end().getIdentifier());
                }
                return builder.toString();
            case NODE:
                final CompleteNode node = (CompleteNode) entity;
                if (node.inEdges() != null)
                {
                    node.inEdges().stream().map(Edge::getIdentifier)
                            .forEach(identifier -> builder.append(identifier + ","));
                }
                builder.append(";");
                if (node.outEdges() != null)
                {
                    node.outEdges().stream().map(Edge::getIdentifier)
                            .forEach(identifier -> builder.append(identifier + ","));
                }
                return builder.toString();
            case RELATION:
                final CompleteRelation relation = (CompleteRelation) entity;
                if (relation.members() != null)
                {
                    final RelationBean bean = relation.members().asBean();
                    builder.append("RelationBean[");
                    for (final RelationBean.RelationBeanItem beanItem : bean)
                    {
                        builder.append("(");
                        builder.append(beanItem.getType());
                        builder.append(",");
                        builder.append(beanItem.getIdentifier());
                        builder.append(",");
                        builder.append(beanItem.getRole());
                        builder.append(")");
                    }
                    builder.append("]");
                }
                return builder.toString();
            default:
                return "";
        }
    }

    /**
     * A helper method that generates a deterministic 64 bit identifier for a given
     * {@link CompleteEntity}. Additionally, this method can be tweaked to prevent negative
     * identifiers.
     * 
     * @param entity
     *            the entity
     * @param allowNegativeIdentifiers
     *            if we want to allow the algorithm to generate negative identifiers
     * @return the identifier
     */
    private long generate(final CompleteEntity<?> entity, final boolean allowNegativeIdentifiers)
    {
        int iterations = 0;
        final int maximumIterations = 1000;

        final String entityString = getBasicPropertyString(entity)
                + getTypeSpecificPropertyString(entity);
        UUID entityHash = UUID.nameUUIDFromBytes(entityString.getBytes());

        long shortHash = entityHash.getMostSignificantBits();
        while (!isHashSafeToUse(shortHash) || (shortHash < 0 && !allowNegativeIdentifiers))
        {
            entityHash = UUID.nameUUIDFromBytes(entityHash.toString().getBytes());
            shortHash = entityHash.getMostSignificantBits();

            if (iterations > maximumIterations)
            {
                throw new CoreException(
                        "Exceeded maximum iterations ({}) when attempting to generate hash",
                        maximumIterations);
            }
            iterations++;
        }

        return shortHash;
    }

    /**
     * Check if the hash falls outside of the unsafe range of possible OSM identifiers.
     *
     * @param hash
     *            the hash to check
     * @return if the hash is in range
     */
    private boolean isHashSafeToUse(final long hash)
    {
        return !(hash < HIGHEST_ATLAS_ID && hash >= LOWEST_ATLAS_ID);
    }
}
