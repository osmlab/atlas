package org.openstreetmap.atlas.exception.change;

/**
 * @author lcram
 */
public enum MergeFailureType
{
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
            "beforeMembers conflict and no beforeView-conflict-capable afterView merging strategy"),
    MISSING_AFTER_VIEW_MERGE_STRATEGY(
            "afterMembers conflict and no afterView merging strategy provided");

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
