package org.openstreetmap.atlas.geography.atlas;

/**
 * Denotes the type of Atlas cut.
 * <ul>
 * <li>Soft-cut: Perform a cut and keep all entities that match the filter or bound. However, bring
 * in additional atlas entities that did not meet the original criteria, in order to satisfy atlas
 * integrity. Example: An Atlas Edge is brought in via boundary or filter match, but one of its
 * Nodes is not. The Node would be brought in, despite not meeting initial criteria, in order to
 * maintain Atlas integrity.</li>
 * <li>Hard-cut-all: Perform a cut, only keep entities that match the bound or filter. If including
 * the item in the final Atlas breaks Atlas integrity, exclude that entity. Example: An Edge is
 * brought in via boundary or filter match, but its start or end Node is omitted. As a result, the
 * Edge is left out of the final Atlas.</li>
 * <li>Hard-cut-relations-only: Perform a soft cut and maintain Atlas integrity for all Atlas Items.
 * For all Relations - perform a hard cut and only include members that satisfy the given predicate
 * or bound. This case will remove Relations altogether if no members satisfy the required
 * conditions.</li>
 * </ul>
 *
 * @author mgostintsev
 */
public enum AtlasCutType
{
    SOFT_CUT,
    HARD_CUT_ALL,
    HARD_CUT_RELATIONS_ONLY;
}
