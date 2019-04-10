package org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.consts;

/**
 * Indicates an operation being performed on a field of a
 * {@link org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity}, like tags, geometry,
 * relations, etc.
 *
 * @author Yazad Khambata
 */
public enum FieldChangeOperation
{
    ADD,
    REMOVE,
    REPLACE,
    OVERWRITE;
}
