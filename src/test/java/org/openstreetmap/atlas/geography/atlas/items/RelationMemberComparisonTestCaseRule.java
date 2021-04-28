package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;

/**
 * Configuration data for {@link RelationMemberComparisonTestCase}
 *
 * @author cstaylor
 */
public class RelationMemberComparisonTestCaseRule extends CoreTestRule
{
    public static final long ID_NAME1 = 1L;
    public static final String ID_NAME1_STRING = "1";

    public static final long ID_NAME2 = 2L;
    public static final String ID_NAME2_STRING = "2";

    public static final long ID_NAME3 = -8902174024992476407L;
    public static final String ID_NAME3_STRING = "-8902174024992476407";

    public static final long ID_NAME4 = 2401632205683965138L;
    public static final String ID_NAME4_STRING = "2401632205683965138";

    @TestAtlas(areas = { @Area(id = ID_NAME1_STRING, tags = { "name=First" }),
            @Area(id = ID_NAME2_STRING, tags = { "name=Second" }),
            @Area(id = ID_NAME3_STRING, tags = { "name=Third" }),
            @Area(id = ID_NAME4_STRING, tags = { "name=Fourth" }) })
    private Atlas atlas;

    public AtlasItem area1()
    {
        return this.atlas.area(ID_NAME1);
    }

    public AtlasItem area2()
    {
        return this.atlas.area(ID_NAME2);
    }

    public AtlasItem area3()
    {
        return this.atlas.area(ID_NAME3);
    }

    public AtlasItem area4()
    {
        return this.atlas.area(ID_NAME4);
    }
}
