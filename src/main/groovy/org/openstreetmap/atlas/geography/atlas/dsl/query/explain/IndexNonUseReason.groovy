package org.openstreetmap.atlas.geography.atlas.dsl.query.explain

/**
 * Reasons for not benefiting from available indexes.
 *
 * @author Yazad Khambata
 */
enum IndexNonUseReason {
    NO_WHERE_CLAUSE,

    FIRST_WHERE_CLAUSE_NEEDS_FULL_SCAN,

    FIRST_WHERE_CLAUSE_USES_NOT_OPERATOR, //special case of FIRST_WHERE_CLAUSE_NEEDS_FULL_SCAN

    WHERE_HAS_OR_USED
}
