package org.openstreetmap.atlas.geography.atlas.sub;

import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Denotes the type of Atlas cut.
 * <ul>
 * <li>Soft-cut: Perform a cut and keep all entities that match the filter or bound. However, bring
 * in additional atlas entities that did not meet the original criteria, in order to satisfy atlas
 * integrity. Example: An Atlas Edge is brought in via boundary or filter match, but one of its
 * Nodes is not. The Node would be brought in, despite not meeting initial criteria, in order to
 * maintain Atlas integrity. Verbose version:
 * <ul>
 * <li>{@link Node}: It is included only if it is matched by the matcher, or if a valid edge (below)
 * has it at one of its ends, or it is pulled in by an {@link Edge} which itself pulled in by a
 * {@link Relation}, matched by the matcher.
 * <li>{@link Edge}: It is included only if it is matched by the matcher or pulled in by a
 * {@link Relation}, matched by the matcher.
 * <li>{@link Area}: It is included only if it is matched by the matcher or pulled in by a
 * {@link Relation}, matched by the matcher.
 * <li>{@link Line}: It is included only if it is matched by the matcher or pulled in by a
 * {@link Relation}, matched by the matcher.
 * <li>{@link Point}: It is included only if it is matched by the matcher or pulled in by a
 * {@link Relation}, matched by the matcher.
 * <li>{@link Relation}: It is included if is matched by matcher or pulled in via another
 * {@link Relation} which was matched by the matcher. To maintain {@link Relation} validity, all of
 * its members will be included in the member list, even if not matched by the given matcher.
 * </ul>
 * </li>
 * <li>Silk-cut: Same as a soft-cut, but always soft-cuts relations and keeps all points on line
 * geometries kept by the slice.
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
    SILK_CUT,
    HARD_CUT_ALL,
    HARD_CUT_RELATIONS_ONLY;
}
