package org.openstreetmap.atlas.exception.change;

/**
 * @author lcram
 */
public enum MergeFailureType
{
    /*
     * These are all root level failures.
     */
    AUTOFAIL_TAG_MERGE("tag Map merge failed due to autofail strategy"),
    AUTOFAIL_LONG_SET_MERGE("Long Set merge failed due to autofail strategy"),
    AUTOFAIL_LONG_SORTED_SET_MERGE("Long SortedSet merge failed due to autofail strategy"),
    AUTOFAIL_LOCATION_MERGE("Location merge failed due to autofail strategy"),
    AUTOFAIL_POLYLINE_MERGE("PolyLine merge failed due to autofail strategy"),
    AUTOFAIL_POLYGON_MERGE("Polygon merge failed due to autofail strategy"),
    AUTOFAIL_LONG_MERGE("Long merge failed due to autofail strategy"),
    SIMPLE_TAG_MERGE_FAIL("simpleTagMerger failed"),
    SIMPLE_LONG_SET_MERGE_FAIL("simpleLongSetMerger failed"),
    SIMPLE_LONG_SORTED_SET_MERGE_FAIL("simpleLongSortedSetMerger failed"),
    SIMPLE_RELATION_BEAN_MERGE_FAIL("simpleRelationBeanMerger failed"),
    DIFF_BASED_RELATION_BEAN_REMOVE_REMOVE_CONFLICT(
            "diffBasedRelationBeanMerger failed due to REMOVE/REMOVE conflict"),
    DIFF_BASED_RELATION_BEAN_ADD_REMOVE_CONFLICT(
            "diffBasedRelationBeanMerger failed due to ADD/REMOVE conflict"),
    DIFF_BASED_RELATION_BEAN_ADD_ADD_CONFLICT(
            "diffBasedRelationBeanMerger failed due to ADD/ADD conflict"),
    DIFF_BASED_TAG_ADD_ADD_CONFLICT("diffBasedTagMerger failed due to ADD/ADD conflict"),
    DIFF_BASED_TAG_ADD_REMOVE_CONFLICT("diffBasedTagMerger failed due to ADD/REMOVE conflict"),
    DIFF_BASED_LONG_MERGE_FAIL("diffBasedLongMerger failed"),
    DIFF_BASED_LOCATION_MERGE_FAIL("diffBasedLocationMerger failed"),
    DIFF_BASED_POLYLINE_MERGE_FAIL("diffBasedPolyLineMerger failed"),
    DIFF_BASED_POLYGON_MERGE_FAIL("diffBasedPolygonMerger failed"),
    CONFLICTING_BEFORE_VIEW_SET_ADD_REMOVE_CONFLICT(
            "conflictingBeforeViewSetMerger failed due to ADD/REMOVE conflict"),
    CONFLICTING_BEFORE_VIEW_RELATION_BEAN_ADD_REMOVE_CONFLICT(
            "conflictingBeforeViewRelationBeanMerger failed due to ADD/REMOVE conflict"),
    MUTUALLY_EXCLUSIVE_ADD_ADD_CONFLICT(
            "diffBasedMutuallyExclusiveMerger failed due to ADD/ADD conflict"),
    FEATURE_CHANGE_INVALID_ADD_REMOVE_MERGE(
            "left and right FeatureChanges disagreed on ChangeType"),
    FEATURE_CHANGE_INVALID_PROPERTIES_MERGE(
            "left and right FeatureChanges disagreed on ID or ItemType"),
    FEATURE_CHANGE_IMBALANCED_BEFORE_VIEW(
            "left and right FeatureChanges did not both have beforeViews"),

    /*
     * These failures occur at the next level up from root. They differentiate between
     * afterView/beforeView merge errors, and do not contain specific member information.
     */
    AFTER_VIEW_NO_BEFORE_VIEW_MERGE_STRATEGY_FAILED(
            "the afterView merging function (that ignores beforeView) failed"),
    AFTER_VIEW_CONSISTENT_BEFORE_VIEW_MERGE_STRATEGY_FAILED(
            "the afterView merging function (that assumes consistent beforeViews) failed"),
    AFTER_VIEW_CONFLICTING_BEFORE_VIEW_MERGE_STRATEGY_FAILED(
            "the afterView merging function (that accounts for conflicting beforeViews) failed"),
    BEFORE_VIEW_MERGE_STRATEGY_FAILED("the beforeView merging function failed"),
    MISSING_BEFORE_VIEW_MERGE_STRATEGY(
            "beforeMembers conflict and no beforeView merging strategy provided"),
    MISSING_AFTER_VIEW_MERGE_STRATEGY_WITH_BEFORE_MEMBER_CONFLICT_HANDLING(
            "beforeMembers conflict and no beforeView-conflict-capable afterView merging strategy provided"),
    MISSING_AFTER_VIEW_MERGE_STRATEGY(
            "afterMembers conflict and no afterView merging strategy provided"),

    /*
     * The generic highest level merge failure.
     */
    HIGHEST_LEVEL_MERGE_FAILURE("the FeatureChange merge failed");

    private final String description;

    MergeFailureType(final String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return this.description;
    }
}
