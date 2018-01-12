package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * Used internally to Atlas for determining the type of a relation
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/type#values", osm = "http://wiki.openstreetmap.org/wiki/Types_of_relation")
public enum RelationTypeTag
{
    MULTIPOLYGON,
    BUILDING,
    RESTRICTION,
    ROUTE,
    ROUTE_MASTER,
    BOUNDARY,
    SITE,
    ASSOCIATEDSTREET,
    PUBLIC_TRANSPORT,
    STREET,
    DESTINATION_SIGN,
    WATERWAY,
    ENFORCEMENT,
    BRIDGE,
    TUNNEL;

    @TagKey
    public static final String KEY = "type";

    public static final String MULTIPOLYGON_TYPE = "multipolygon";
    public static final String MULTIPOLYGON_ROLE_INNER = "inner";
    public static final String MULTIPOLYGON_ROLE_OUTER = "outer";

    public static final String BUILDING_ROLE_OUTLINE = "outline";
    public static final String BUILDING_ROLE_PART = "part";

    public static final String RESTRICTION_ROLE_FROM = "from";
    public static final String RESTRICTION_ROLE_VIA = "via";
    public static final String RESTRICTION_ROLE_TO = "to";

    public static final String ADMINISTRATIVE_BOUNDARY_ROLE_SUB_AREA = "subarea";
}
