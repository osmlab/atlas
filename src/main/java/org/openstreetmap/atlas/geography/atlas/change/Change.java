package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.serializer.ChangeGeoJsonSerializer;
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
 */
public class Change implements Located, Serializable
{
    private static final long serialVersionUID = 1048481626851547987L;
    private static final Logger logger = LoggerFactory.getLogger(Change.class);
    private static final AtomicInteger CHANGE_IDENTIFIER_FACTORY = new AtomicInteger();

    private final List<FeatureChange> featureChanges;
    private final Map<Tuple<ItemType, Long>, Integer> identifierToIndex;
    private Rectangle bounds;
    private final int identifier;
    private String name;

    protected Change()
    {
        this.featureChanges = new ArrayList<>();
        this.identifierToIndex = new HashMap<>();
        this.identifier = CHANGE_IDENTIFIER_FACTORY.getAndIncrement();
    }

    List<FeatureChange> getFeatureChanges()
    {
        return this.featureChanges;
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
        final Tuple<ItemType, Long> key = new Tuple<>(itemType, identifier);
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

    public int getIdentifier()
    {
        return this.identifier;
    }

    public String getName()
    {
        if (this.name == null)
        {
            return String.valueOf(this.getIdentifier());
        }
        else
        {
            return this.name;
        }
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

    public String toJson()
    {
        return new ChangeGeoJsonSerializer().convert(this);
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

    protected void add(final FeatureChange featureChange)
    {
        final int currentIndex = this.featureChanges.size();
        final Tuple<ItemType, Long> key = new Tuple<>(featureChange.getItemType(),
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
            this.bounds = this.bounds.combine(featureBounds);
        }
        else
        {
            this.bounds = featureBounds;
        }
    }
}
