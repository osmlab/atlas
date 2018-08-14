package org.openstreetmap.atlas.geography.atlas.delta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * A single Atlas diff
 *
 * @author matthieun
 */
public class Diff implements Comparable<Diff>, Serializable
{
    /**
     * @author matthieun
     */
    public enum DiffReason
    {
        ADDED,
        REMOVED,
        TAGS,
        GEOMETRY_OR_TOPOLOGY,
        RELATION_MEMBER,
        RELATION_TOPOLOGY;
    }

    /**
     * @author matthieun
     */
    public enum DiffType
    {
        ADDED,
        CHANGED,
        REMOVED;
    }

    private static final long serialVersionUID = -1798331824716201841L;

    private final ItemType itemType;
    private final DiffType diffType;
    private final DiffReason diffReason;
    private final Atlas before;
    private final Atlas after;
    private final long identifier;

    /**
     * Similar to the regular toString, but attempts to make the diff string more friendly to human
     * readers.
     *
     * @param diffs
     *            An {@link Iterable} of {@link Diff}
     * @return the human readable diff string
     */
    public static String toDiffViewFriendlyString(final Iterable<Diff> diffs)
    {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder builder = new StringBuilder();
        builder.append("Diffset {");
        final StringList list = new StringList();
        for (final Diff diff : diffs)
        {
            list.add(newLine + diff.toDiffViewFriendlyString());
        }
        builder.append(list.join(newLine));
        builder.append(newLine + "}");
        return builder.toString();
    }

    /**
     * @param diffs
     *            An {@link Iterable} of {@link Diff}
     * @return A GeoJSON String representation of all the {@link Diff} items in the {@link Iterable}
     */
    public static String toGeoJson(final Iterable<Diff> diffs)
    {
        return toGeoJson(diffs, val -> true);
    }

    /**
     * @param diffs
     *            An {@link Iterable} of {@link Diff}
     * @param filter
     *            The filter to apply to the diff
     * @return A GeoJSON String representation of all the {@link Diff} items in the {@link Iterable}
     *         , which match the filter.
     */
    public static String toGeoJson(final Iterable<Diff> diffs, final Predicate<Diff> filter)
    {
        return new GeoJsonBuilder().create(Iterables.stream(diffs)
                .filter(diff -> diff.getItemType() != ItemType.RELATION)
                .filter(filter)
                .flatMap(Diff::processDiff)
                .collect())
                .jsonObject()
                .toString();
    }

    private static List<LocationIterableProperties> processDiff(final Diff diff)
    {
        final List<LocationIterableProperties> items = new ArrayList<>();

        final AtlasEntity beforeEntity = diff.getBeforeEntity();
        // null if it is ADDED
        if (beforeEntity != null)
        {
            final Map<String, String> beforeTags = beforeEntity.getTags();
            beforeTags.put("diff", "BEFORE");
            beforeTags.put("diff:type", diff.getDiffType().name());
            beforeTags.put("dif:reason", diff.getDiffReason().name());
            if (diff.getItemType() == ItemType.RELATION)
            {
                items.addAll(processRelationForGeoJson((Relation) beforeEntity, beforeTags));
            }
            else
            {
                items.add(new LocationIterableProperties(((AtlasItem) beforeEntity).getRawGeometry(), beforeTags));
            }
        }

        final AtlasEntity afterEntity = diff.getAfterEntity();
        // null iff it is REMOVED
        if (afterEntity != null)
        {
            final Map<String, String> afterTags = afterEntity.getTags();
            afterTags.put("diff", "AFTER");
            afterTags.put("diff:type", diff.getDiffType().name());
            afterTags.put("dif:reason", diff.getDiffReason().name());
            if (diff.getItemType() == ItemType.RELATION)
            {
                items.addAll(processRelationForGeoJson((Relation) afterEntity, afterTags));
            }
            else
            {
                items.add(new LocationIterableProperties(((AtlasItem) afterEntity).getRawGeometry(), afterTags));
            }
        }

        return items;
    }

    /**
     * @param diffs
     *            An {@link Iterable} of {@link Diff}
     * @return A GeoJSON String representation of all the {@link AtlasItem}s that are a
     *         {@link RelationMember} in a {@link Diff} {@link Relation}
     */
    public static String toRelationsGeoJson(final Iterable<Diff> diffs)
    {
        return toRelationsGeoJson(diffs, val -> true);
    }

    /**
     * @param diffs
     *            An {@link Iterable} of {@link Diff}
     * @param filter
     *            The filter to apply to the diff
     * @return A GeoJSON String representation of all the {@link AtlasItem}s that are a
     *         {@link RelationMember} in a {@link Diff} {@link Relation}, which match the filter.
     */
    public static String toRelationsGeoJson(final Iterable<Diff> diffs,
            final Predicate<Diff> filter)
    {
        return new GeoJsonBuilder().create(Iterables.stream(diffs)
                .filter(diff -> diff.getItemType() == ItemType.RELATION)
                .filter(filter)
                .flatMap(Diff::processDiff)
                .collect())
                .jsonObject()
                .toString();
    }

    /**
     * @param diffs
     *            An {@link Iterable} of {@link Diff}
     * @return A String representation of all the {@link Diff} items in the {@link Iterable}
     */
    public static String toString(final Iterable<Diff> diffs)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Diffs: ");
        final StringList list = new StringList();
        for (final Diff diff : diffs)
        {
            list.add("\n\t" + diff.toString());
        }
        builder.append(list.join(", "));
        builder.append("\n]");
        return builder.toString();
    }

    private static List<LocationIterableProperties> processRelationForGeoJson(
            final Relation relation, final Map<String, String> parentTags)
    {
        final Map<String, String> relationTags = relation.getTags();
        final Map<String, String> modifiedRelationTags = new HashMap<>(parentTags);
        for (final String key : relationTags.keySet())
        {
            modifiedRelationTags.put("[REL_ID:" + relation.getIdentifier() + "]" + key,
                    relationTags.get(key));
        }
        final List<LocationIterableProperties> result = new ArrayList<>();
        for (final RelationMember member : relation.members())
        {
            if (member.getEntity() instanceof Relation)
            {
                final Relation subRelation = (Relation) member.getEntity();
                result.addAll(processRelationForGeoJson(subRelation, modifiedRelationTags));
            }
            else
            {
                final AtlasItem item = (AtlasItem) member.getEntity();
                final Map<String, String> modifiedTags = item.getTags();
                modifiedTags.putAll(modifiedRelationTags);
                result.add(new LocationIterableProperties(item.getRawGeometry(), modifiedTags));
            }
        }
        return result;
    }

    /**
     * Construct
     *
     * @param itemType
     *            The type of the entity that this {@link Diff} represents
     * @param diffType
     *            The type of this {@link Diff}, among "ADDED", "REMOVED" and "CHANGED"
     * @param diffReason
     *            The reason of this {@link Diff}
     * @param before
     *            The before {@link Atlas}, i.e. the older one
     * @param after
     *            The after {@link Atlas}, i.e. the newer one
     * @param identifier
     *            The identifier if the entity that this {@link Diff} represents.
     */
    public Diff(final ItemType itemType, final DiffType diffType, final DiffReason diffReason,
                final Atlas before, final Atlas after, final long identifier)
    {
        this.itemType = itemType;
        this.diffType = diffType;
        this.diffReason = diffReason;
        this.before = before;
        this.after = after;
        this.identifier = identifier;
    }

    @Override
    public int compareTo(final Diff other)
    {
        if (this.getDiffType() != other.getDiffType())
        {
            return this.getDiffType().compareTo(other.getDiffType());
        }
        if (this.getItemType() != other.getItemType())
        {
            return this.itemType.compareTo(other.getItemType());
        }
        final long deltaIdentifier = this.getIdentifier() - other.getIdentifier();
        return deltaIdentifier > 0 ? 1 : deltaIdentifier == 0 ? 0 : -1;
    }

    /**
     * @return The after {@link Atlas}, i.e. the newer one
     */
    public Atlas getAfter()
    {
        return this.after;
    }

    /**
     * @return The entity this {@link Diff} represents in the newer Atlas. null if this Diff is of
     *         type "REMOVED"
     */
    public AtlasEntity getAfterEntity()
    {
        return this.itemType.entityForIdentifier(this.after, this.identifier);
    }

    /**
     * @return The before {@link Atlas}, i.e. the older one
     */
    public Atlas getBefore()
    {
        return this.before;
    }

    /**
     * @return The entity this {@link Diff} represents in the older Atlas. null if this Diff is of
     *         type "ADDED"
     */
    public AtlasEntity getBeforeEntity()
    {
        return this.itemType.entityForIdentifier(this.before, this.identifier);
    }

    /**
     * @return The reason for this diff
     */
    public DiffReason getDiffReason()
    {
        return this.diffReason;
    }

    /**
     * @return The type of this {@link Diff}, among "ADDED", "REMOVED" and "CHANGED"
     */
    public DiffType getDiffType()
    {
        return this.diffType;
    }

    /**
     * @return The identifier of the entity that this {@link Diff} represents.
     */
    public long getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return The type of the entity that this {@link Diff} represents
     */
    public ItemType getItemType()
    {
        return this.itemType;
    }

    /**
     * @return True if this diff is of type ADDED
     */
    public boolean isAdded()
    {
        return DiffType.ADDED == this.getDiffType();
    }

    /**
     * @return True if this diff is of type CHANGED
     */
    public boolean isChanged()
    {
        return DiffType.CHANGED == this.getDiffType();
    }

    /**
     * @return True if this diff is of type REMOVED
     */
    public boolean isRemoved()
    {
        return DiffType.REMOVED == this.getDiffType();
    }

    /**
     * Similar to the regular toString method. However, this version returns the {@link Diff} with
     * an attempt at being more friendly to diff readouts.
     *
     * @return the string
     */
    public String toDiffViewFriendlyString()
    {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder builder = new StringBuilder();
        builder.append("Diff {");
        builder.append(newLine);
        builder.append("diffType: " + this.diffType);
        builder.append(newLine);
        builder.append("diffReason: " + this.diffReason);
        builder.append(newLine);
        builder.append("Entity = " + this.itemType);
        builder.append(newLine);
        builder.append("ID = " + this.identifier);
        builder.append(newLine);
        if (this.getBeforeEntity() != null)
        {
            builder.append(this.getBeforeEntity().toDiffViewFriendlyString());
        }
        else
        {
            builder.append("null");
        }
        builder.append(newLine);
        builder.append(" -> ");
        builder.append(newLine);
        if (this.getAfterEntity() != null)
        {
            builder.append(this.getAfterEntity().toDiffViewFriendlyString());
        }
        else
        {
            builder.append("null");
        }
        builder.append(newLine);
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Diff: ");
        builder.append(this.diffType);
        builder.append(", Entity = {");
        builder.append(this.itemType);
        builder.append(", ID = ");
        builder.append(this.identifier);
        builder.append(", ");
        builder.append(this.getBeforeEntity());
        builder.append(" -> ");
        builder.append(this.getAfterEntity());
        builder.append("}]");
        return builder.toString();
    }
}
