package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.index.RTree;
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

    private final SortedMap<Long, FeatureChange> indexToFeatureChange;
    private final Map<Tuple<ItemType, Long>, Long> identifierToIndex;
    private final MultiMap<Location, Long> locationToIndex;
    private Rectangle bounds;
    private transient volatile RTree<FeatureChange> spatialIndex;

    protected Change()
    {
        this.indexToFeatureChange = new TreeMap<>();
        this.identifierToIndex = new HashMap<>();
        this.locationToIndex = new MultiMap<>();
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

    protected void addFeatureChange(final FeatureChange featureChange)
    {
        final long currentIndex = this.indexToFeatureChange.lastKey() + 1;
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
        return Optional.ofNullable(this.indexToFeatureChange
                .get(this.identifierToIndex.get(new Tuple<>(itemType, identifier))));
    }

    protected Stream<FeatureChange> changesFor(final ItemType itemType)
    {
        return this.identifierToIndex.keySet().stream()
                .filter(tuple -> tuple.getFirst() == itemType)
                .map(tuple -> this.indexToFeatureChange.get(this.identifierToIndex.get(tuple)));
    }

    private RTree<FeatureChange> getSpatialIndex()
    {
        // Needs to be lazily generated
        throw new UnsupportedOperationException();
    }
}
