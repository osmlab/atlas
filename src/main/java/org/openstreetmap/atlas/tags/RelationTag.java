package org.openstreetmap.atlas.tags;

/**
 * @author matthieun
 */
public final class RelationTag
{
    public static final String TYPE = "type";
    public static final String YES = "yes";

    public static final String MULTIPOLYGON = "multipolygon";
    public static final String MULTIPOLYGON_ROLE_INNER = "inner";
    public static final String MULTIPOLYGON_ROLE_OUTER = "outer";

    public static final String BUILDING = BuildingTag.KEY;
    public static final String BUILDING_ROLE_OUTLINE = "outline";
    public static final String BUILDING_ROLE_PART = "part";

    private RelationTag()
    {
    }
}
