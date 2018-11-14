package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.validators.ChangeValidator;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

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
    private static final AtomicInteger CHANGE_IDENTIFIER_FACTORY = new AtomicInteger();

    private final SortedMap<Long, FeatureChange> indexToFeatureChange;
    private final Map<Tuple<ItemType, Long>, Long> identifierToIndex;
    private final MultiMap<Location, Long> locationToIndex;
    private Rectangle bounds;
    private transient volatile RTree<FeatureChange> spatialIndex;
    private final int identifier;
    private String name;

    protected Change()
    {
        this.indexToFeatureChange = new TreeMap<>();
        this.identifierToIndex = new HashMap<>();
        this.locationToIndex = new MultiMap<>();
        this.identifier = CHANGE_IDENTIFIER_FACTORY.getAndIncrement();
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    public Collection<FeatureChange> getFeatureChanges()
    {
        return this.indexToFeatureChange.values();
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

    @Override
    public String toString()
    {
        final StringList split = new StringList();
        final StringBuilder builder = new StringBuilder();
        this.indexToFeatureChange
                .forEach((index, featureChange) -> split.add(index + " - " + featureChange));
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
        final long currentIndex = this.indexToFeatureChange.isEmpty() ? 0
                : this.indexToFeatureChange.lastKey() + 1;
        final Tuple<ItemType, Long> key = new Tuple<>(featureChange.getItemType(),
                featureChange.getIdentifier());
        if (!this.identifierToIndex.containsKey(key))
        {
            this.identifierToIndex.put(key, currentIndex);
        }
        else
        {
            throw new CoreException(
                    "Change already has a feature change {}. Adding {} is not allowed.",
                    this.identifierToIndex.get(key), featureChange);
        }
        this.indexToFeatureChange.put(currentIndex, featureChange);
        final AtlasEntity reference = featureChange.getReference();
        if (reference instanceof LocationItem)
        {
            this.locationToIndex.add(((LocationItem) reference).getLocation(), currentIndex);
        }
        final Rectangle featureBounds = featureChange.bounds();
        if (this.bounds != null)
        {
            this.bounds = this.bounds.combine(featureBounds);
        }
        else
        {
            this.bounds = featureBounds;
        }
    }

    protected Optional<FeatureChange> changeFor(final ItemType itemType, final Long identifier)
    {
        final Tuple<ItemType, Long> key = new Tuple<>(itemType, identifier);
        if (!this.identifierToIndex.containsKey(key))
        {
            return Optional.empty();
        }
        return Optional.of(this.indexToFeatureChange.get(this.identifierToIndex.get(key)));
    }

    protected Stream<FeatureChange> changesFor(final ItemType itemType)
    {
        return this.identifierToIndex.keySet().stream()
                .filter(tuple -> tuple.getFirst() == itemType)
                .map(tuple -> this.indexToFeatureChange.get(this.identifierToIndex.get(tuple)));
    }

    protected void validate()
    {
        new ChangeValidator(this).validate();
    }

    private RTree<FeatureChange> getSpatialIndex()
    {
        // Needs to be lazily generated
        throw new UnsupportedOperationException();
    }
}
