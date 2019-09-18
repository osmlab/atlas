package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.change.serializer.ChangeGeoJsonSerializer;
import org.openstreetmap.atlas.geography.atlas.change.serializer.FeatureChangeGeoJsonSerializer;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.PrettifyStringFormat;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A change that can be applied to an {@link Atlas} to generate a {@link ChangeAtlas}.
 * <p>
 * It contains a collection of {@link FeatureChange} objects, which describe the changes.
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public class Change implements Located, Serializable
{
    private static final long serialVersionUID = 1048481626851547987L;
    private static final Logger logger = LoggerFactory.getLogger(Change.class);
    private static final AtomicInteger CHANGE_IDENTIFIER_FACTORY = new AtomicInteger();

    private final List<FeatureChange> featureChanges;
    private final Map<AtlasEntityKey, Integer> identifierToIndex;
    private final int identifier;
    private Rectangle bounds;
    private String name;

    /**
     * Merge {@link FeatureChange}s inside {@link Change} objects and create a
     * {@link Change#newInstance()} with the merged {@link FeatureChange}s. The
     * {@link #merge(Change...)} is guided by groupings based on {@link FeatureChangeMergeGroup}.
     *
     * @param changeInstances
     *            - the {@link Change} instances to merge.
     * @return - A {@link #newInstance()} of Change with {@link FeatureChange}s
     *         {@link FeatureChange#merge(FeatureChange)}-ed.
     */
    public static Change merge(final Change... changeInstances)
    {
        final FeatureChange[] mergedFeatureChanges = Arrays.stream(changeInstances)
                .flatMap(Change::changes)
                .collect(Collectors.groupingBy(FeatureChangeMergeGroup::from, LinkedHashMap::new,
                        Collectors.reducing(FeatureChange::merge)))
                .values().stream().map(Optional::get).toArray(FeatureChange[]::new);

        return Change.newInstance().withName("Merged Change").addAll(mergedFeatureChanges);
    }

    public static Change newInstance()
    {
        return new Change();
    }

    protected Change()
    {
        this.featureChanges = new ArrayList<>();
        this.identifierToIndex = new HashMap<>();
        this.identifier = CHANGE_IDENTIFIER_FACTORY.getAndIncrement();
    }

    public Map<AtlasEntityKey, FeatureChange> allChangesMappedByAtlasEntityKey()
    {
        return changes()
                .map(featureChange -> Tuple.createTuple(AtlasEntityKey.from(featureChange),
                        featureChange))
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    public int changeCount()
    {
        return this.featureChanges.size();
    }

    public Optional<FeatureChange> changeFor(final ItemType itemType, final Long identifier)
    {
        final AtlasEntityKey key = AtlasEntityKey.from(itemType, identifier);
        if (!this.identifierToIndex.containsKey(key))
        {
            return Optional.empty();
        }
        return Optional.ofNullable(this.featureChanges.get(this.identifierToIndex.get(key)));
    }

    public Stream<FeatureChange> changes()
    {
        return this.featureChanges.stream();
    }

    public Stream<FeatureChange> changesFor(final ItemType itemType)
    {
        return this.identifierToIndex.keySet().stream()
                .filter(tuple -> tuple.getFirst() == itemType)
                .map(tuple -> this.featureChanges.get(this.identifierToIndex.get(tuple)));
    }

    /**
     * An Object{@link #equals(Object)} implementation based on {@link #featureChanges} in the
     * {@link Change} objects being compared.
     *
     * @param other
     *            - the object to compare.
     * @return boolean - true if the objects are equal
     * @see Objects#equals(Object, Object)
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object other)
    {
        // self check
        if (this == other)
        {
            return true;
        }
        // null check
        if (other == null)
        {
            return false;
        }
        // type check and cast
        if (getClass() != other.getClass())
        {
            return false;
        }

        final Change that = (Change) other;

        return Objects.equals(this.featureChanges, that.featureChanges);
    }

    public int getIdentifier()
    {
        return this.identifier;
    }

    public String getName()
    {
        return Optional.ofNullable(this.name).orElse(String.valueOf(this.getIdentifier()));
    }

    /**
     * An Object{@link #hashCode()} implementation based on {@link #featureChanges} in the
     * {@link Change}.
     *
     * @return - the hash code.
     * @see Objects#hashCode(Object)
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.featureChanges);
    }

    /**
     * Transform this {@link Change} into a pretty string. This will use the pretty strings for
     * {@link CompleteEntity} classes that make up this {@link Change}'s constituent
     * {@link FeatureChange}s.
     *
     * @param featureChangeFormat
     *            the format type for the the constituent {@link FeatureChange}s
     * @param completeEntityFormat
     *            the format type for the constituent {@link CompleteEntity}s
     * @param truncate
     *            whether or not to truncate long fields
     * @return the pretty string
     */
    public String prettify(final PrettifyStringFormat featureChangeFormat,
            final PrettifyStringFormat completeEntityFormat, final boolean truncate)
    {
        final StringBuilder builder = new StringBuilder();

        builder.append(this.getClass().getSimpleName() + " [");
        builder.append("\n");
        for (final FeatureChange featureChange : this.featureChanges)
        {
            builder.append(
                    featureChange.prettify(featureChangeFormat, completeEntityFormat, truncate));
            builder.append("\n");
        }
        builder.append("]");

        return builder.toString();
    }

    /**
     * Transform this {@link Change} into a pretty string. This will use the pretty strings for
     * {@link CompleteEntity} classes that make up this {@link Change}'s constituent
     * {@link FeatureChange}s.
     *
     * @param featureChangeFormat
     *            the format type for the the constituent {@link FeatureChange}s
     * @param completeEntityFormat
     *            the format type for the constituent {@link CompleteEntity}s
     * @return the pretty string
     */
    public String prettify(final PrettifyStringFormat featureChangeFormat,
            final PrettifyStringFormat completeEntityFormat)
    {
        return this.prettify(featureChangeFormat, completeEntityFormat, true);
    }

    /**
     * Save a JSON representation of that feature change.
     *
     * @param resource
     *            The {@link WritableResource} to save the JSON to.
     */
    public void save(final WritableResource resource)
    {
        new ChangeGeoJsonSerializer().accept(this, resource);
    }

    /**
     * Save a JSON representation of that change.
     *
     * @param resource
     *            The {@link WritableResource} to save the JSON to.
     * @param showDescription
     *            whether or not to show the {@link ChangeDescription} for each component
     *            {@link FeatureChange}
     */
    public void save(final WritableResource resource, final boolean showDescription)
    {
        new ChangeGeoJsonSerializer(true, showDescription).accept(this, resource);
    }

    public String toJson()
    {
        return new ChangeGeoJsonSerializer().convert(this);
    }

    public String toJson(final boolean showDescription)
    {
        return new ChangeGeoJsonSerializer(true, showDescription).convert(this);
    }

    public String toLineDelimitedFeatureChanges()
    {
        final StringBuilder builder = new StringBuilder();
        final FeatureChangeGeoJsonSerializer serializer = new FeatureChangeGeoJsonSerializer(false);

        for (final FeatureChange featureChange : this.featureChanges)
        {
            builder.append(serializer.apply(featureChange) + "\n");
        }
        return builder.toString();
    }

    @Override
    public String toString()
    {
        final StringList split = new StringList();
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < this.featureChanges.size(); index++)
        {
            split.add(index + " - " + this.featureChanges.get(index));
        }
        builder.append("[Change:");
        builder.append(System.lineSeparator());
        builder.append(split.join(System.lineSeparator()));
        builder.append(System.lineSeparator());
        builder.append("]");
        return builder.toString();
    }

    public Change withName(final String name)
    {
        this.name = name;
        return this;
    }

    protected Change add(final FeatureChange featureChange)
    {
        final int currentIndex = this.featureChanges.size();
        final AtlasEntityKey key = AtlasEntityKey.from(featureChange.getItemType(),
                featureChange.getIdentifier());

        FeatureChange chosen = featureChange;
        if (!this.identifierToIndex.containsKey(key))
        {
            this.identifierToIndex.put(key, currentIndex);
            this.featureChanges.add(featureChange);
        }
        else
        {
            final int existingIndex = this.identifierToIndex.get(key);
            final FeatureChange existing = this.featureChanges.get(existingIndex);
            logger.trace(
                    "Change already has a similar feature change. Triggered a merge attempt! Existing: {}; New: {}",
                    existing, featureChange);
            chosen = existing.merge(featureChange);
            this.featureChanges.set(existingIndex, chosen);
        }
        final Rectangle featureBounds = chosen.bounds();
        if (this.bounds != null)
        {
            this.bounds = featureBounds != null ? this.bounds.combine(featureBounds) : this.bounds;
        }
        else
        {
            this.bounds = featureBounds;
        }

        return this;
    }

    protected Change addAll(final FeatureChange... featureChanges)
    {
        Arrays.stream(featureChanges).forEach(this::add);
        return this;
    }

    List<FeatureChange> getFeatureChanges()
    {
        return this.featureChanges;
    }
}
