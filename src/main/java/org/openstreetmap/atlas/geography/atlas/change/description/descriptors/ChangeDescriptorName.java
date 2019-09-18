package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

/**
 * The various values of the name field in the {@link ChangeDescriptor} JSON serialization. This
 * enum mostly exists to prevent us from having to hardcode strings all over the place.
 * 
 * @author lcram
 */
public enum ChangeDescriptorName
{
    TAG,
    GEOMETRY,
    PARENT_RELATION,
    RELATION_MEMBER,
    IN_EDGE,
    OUT_EDGE,
    START_NODE,
    END_NODE
}
