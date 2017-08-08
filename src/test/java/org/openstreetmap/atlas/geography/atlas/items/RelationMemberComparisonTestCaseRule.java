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

    @TestAtlas(areas = { @Area(id = ID_NAME1_STRING, tags = { "name=First" }),
            @Area(id = ID_NAME2_STRING, tags = { "name=Second" }) })
    private Atlas atlas;

    public AtlasItem area1()
    {
        return this.atlas.area(ID_NAME1);
    }

    public AtlasItem area2()
    {
        return this.atlas.area(ID_NAME2);
    }
}
