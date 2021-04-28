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
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
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
    /**
     * A config class for {@link EntityIdentifierGenerator} to set various configuration parameters.
     * 
     * @author lcram
     */
    public static class Configuration
    {
        private boolean useGeometry;
        private boolean useTags;
        private boolean useRelationMembers;

        public Configuration()
        {
            this.useGeometry = false;
            this.useTags = false;
            this.useRelationMembers = false;
        }

        public Configuration excludeGeometry()
        {
            this.useGeometry = false;
            return this;
        }

        public Configuration excludeRelationMembers()
        {
            this.useRelationMembers = false;
            return this;
        }

        public Configuration excludeTags()
        {
            this.useTags = false;
            return this;
        }

        /**
         * Get an {@link EntityIdentifierGenerator} built with this {@link Configuration}.
         * 
         * @return a configured {@link EntityIdentifierGenerator}
         */
        public EntityIdentifierGenerator getGenerator()
        {
            return new EntityIdentifierGenerator(this);
        }

        /**
         * This {@link Configuration} is empty if all fields are false. Empty {@link Configuration}s
         * are generally not valid.
         *
         * @return if this {@link Configuration} is empty
         */
        public boolean isEmpty()
        {
            return !this.useGeometry && !this.useTags && !this.useRelationMembers;
        }

        /**
         * Check if this {@link Configuration} is non-relation invariant. A non-relation invariant
         * {@link Configuration} is one that will generate the same ID for any non-relation type
         * entity.
         *
         * @return if this {@link Configuration} is non-relation invariant.
         */
        public boolean isNonRelationInvariant()
        {
            return !this.useGeometry && !this.useTags;
        }

        public boolean isUsingGeometry()
        {
            return this.useGeometry;
        }

        public boolean isUsingRelationMembers()
        {
            return this.useRelationMembers;
        }

        public boolean isUsingTags()
        {
            return this.useTags;
        }

        /**
         * Set all fields to true. This imitates the behaviour of the default
         * {@link EntityIdentifierGenerator} constructor without any configuration.
         *
         * @return a {@link Configuration} set with the defaults
         */
        public Configuration useDefaults()
        {
            this.useGeometry = true;
            this.useTags = true;
            this.useRelationMembers = true;
            return this;
        }

        public Configuration useGeometry()
        {
            this.useGeometry = true;
            return this;
        }

        public Configuration useRelationMembers()
        {
            this.useRelationMembers = true;
            return this;
        }

        public Configuration useTags()
        {
            this.useTags = true;
            return this;
        }
    }

    private static final long HIGHEST_ATLAS_ID = 9999999999999999L;
    private static final long LOWEST_ATLAS_ID = -9999999999999999L;

    private final Configuration configuration;

    public EntityIdentifierGenerator()
    {
        this(new Configuration().useDefaults());
    }

    public EntityIdentifierGenerator(final Configuration configuration)
    {
        this.configuration = configuration;
    }

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
        if (entity.getType() == ItemType.EDGE)
        {
            throw new IllegalArgumentException(
                    "For type EDGE, please use generatePositiveIdentifierForEdge");
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
        final StringBuilder builder = new StringBuilder();

        if (this.configuration.isUsingGeometry())
        {
            final String wkt = entity.toWkt();
            if (wkt == null && !(entity instanceof CompleteRelation))
            {
                throw new CoreException("Geometry must be set for entity {}", entity.prettify());
            }
            builder.append(wkt);
        }
        if (this.configuration.isUsingTags())
        {
            final Map<String, String> tags = entity.getTags();
            if (tags == null)
            {
                throw new CoreException("Tags must be set for entity {}", entity.prettify());
            }
            final SortedSet<String> sortedTags = tags.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toCollection(TreeSet::new));
            final String tagString = new StringList(sortedTags).join(",");
            builder.append(";");
            builder.append(tagString);
        }

        return builder.toString();
    }

    /**
     * Given some {@link CompleteEntity}, compute a string made up of concatenated type specific
     * entity properties. Currently this is only relevant for {@link Relation}s.
     *
     * @param entity
     *            the {@link CompleteEntity} to string-ify
     * @return the property string
     */
    String getTypeSpecificPropertyString(final CompleteEntity<?> entity)
    {
        if (entity.getType() != ItemType.RELATION)
        {
            return "";
        }

        if (this.configuration.isUsingRelationMembers())
        {
            final StringBuilder builder = new StringBuilder();
            builder.append(";");

            final CompleteRelation relation = (CompleteRelation) entity;

            if (relation.members() == null)
            {
                throw new CoreException("Relation members must be set for entity {}",
                        entity.prettify());
            }

            final RelationBean bean = relation.members().asBean();
            builder.append("RelationBean[");
            // Here use sorted list to ensure determinism
            for (final RelationBean.RelationBeanItem beanItem : bean.asSortedList())
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
            return builder.toString();
        }

        return "";
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
        if (this.configuration.isEmpty())
        {
            throw new CoreException(
                    "EntityIdentifierGenerator.Configuration was empty! Please set at least one of geometry, tags, or relation members.");
        }

        if (entity.getType() != ItemType.RELATION && this.configuration.isNonRelationInvariant())
        {
            throw new CoreException(
                    "EntityIdentifierGenerator.Configuration was non-relation invariant! Please set at least one of geometry or tags to generate IDs for non-relation type entities.");
        }

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
                /*
                 * If this happens, we have a problem. It means that, within 1000 tries, we could
                 * not generate an ID that was both safe to use (outside of OSM ID boundary) and
                 * within the sign constraints (positive vs. negative allowed IDs). Realistically,
                 * this should never happen. If for some reason it did, a possible solution would be
                 * to simply bump up the iteration threshold.
                 */
                throw new CoreException(
                        "Exceeded maximum iterations ({}) when attempting to generate hash for {}",
                        maximumIterations, entity.prettify());
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
        return hash > HIGHEST_ATLAS_ID || hash < LOWEST_ATLAS_ID;
    }
}
