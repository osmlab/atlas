package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BinaryOperator;

import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * A utility class to store the various merger operations utilized by {@link FeatureChange}.
 *
 * @author lcram
 */
public final class FeatureChangeMergers
{
    static final BinaryOperator<Map<String, String>> tagMerger = Maps::withMaps;

    static final BinaryOperator<Set<Long>> directReferenceMerger = Sets::withSets;

    static final BinaryOperator<SortedSet<Long>> directReferenceMergerSorted = Sets::withSortedSets;

    static final BinaryOperator<SortedSet<Long>> directReferenceMergerLooseSorted = (left,
            right) -> Sets.withSortedSets(false, left, right);

    static final BinaryOperator<Set<Long>> directReferenceMergerLoose = (left, right) -> Sets
            .withSets(false, left, right);

    static final BinaryOperator<RelationBean> relationBeanMerger = RelationBean::merge;

    private FeatureChangeMergers()
    {

    }
}
