package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * A change that can be applied to an {@link Atlas} to generate a {@link ChangeAtlas}
 *
 * @author matthieun
 */
public class Change implements Located, Serializable
{
    private static final long serialVersionUID = 1048481626851547987L;

    private final SortedMap<Long, FeatureChange> indexToFeatureChange;
    private final MultiMap<Long, Long> identifierToIndex;
    private final MultiMap<Location, Long> locationToIndex;
    private Rectangle bounds;
    private transient volatile RTree<FeatureChange> spatialIndex;

    protected Change()
    {
        this.indexToFeatureChange = new TreeMap<>();
        this.identifierToIndex = new MultiMap<>();
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
        this.indexToFeatureChange.put(currentIndex, featureChange);
        this.identifierToIndex.add(featureChange.getIdentifier(), currentIndex);
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

    private RTree<FeatureChange> getSpatialIndex()
    {
        throw new UnsupportedOperationException();
    }
}
