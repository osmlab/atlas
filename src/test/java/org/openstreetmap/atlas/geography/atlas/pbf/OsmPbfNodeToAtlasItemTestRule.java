package org.openstreetmap.atlas.geography.atlas.pbf;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link OsmPbfNodeToAtlasItemTest} test cases.
 *
 * @author mgostintsev
 */
public class OsmPbfNodeToAtlasItemTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "noRelationNoTagsNoIntersectionAtlas.osm")
    private Atlas atlasWithoutRelationNoTagsNoIntersection;

    @TestAtlas(loadFromJosmOsmResource = "partOfRelationNoTagsAtIntersectionAtlas.osm")
    private Atlas atlasPartOfRelationNoTagsAtIntersection;

    @TestAtlas(loadFromJosmOsmResource = "partOfRelationWithTagsNoIntersectionAtlas.osm")
    private Atlas atlasPartOfRelationWithTagsNoIntersection;

    @TestAtlas(loadFromJosmOsmResource = "partOfRelationWithTagsAtIntersectionAtlas.osm")
    private Atlas atlasPartOfRelationWithTagsAtIntersection;

    @TestAtlas(loadFromJosmOsmResource = "noRelationWithTagsNoIntersectionAtlas.osm")
    private Atlas atlasWithoutRelationWithTagsNoIntersection;

    @TestAtlas(loadFromJosmOsmResource = "noRelationNoTagsAtIntersectionAtlas.osm")
    private Atlas atlasWithoutRelationNoTagsAtIntersection;

    @TestAtlas(loadFromJosmOsmResource = "noRelationWithTagsAtIntersectionAtlas.osm")
    private Atlas atlasWithoutRelationWithTagsAtIntersection;

    @TestAtlas(loadFromJosmOsmResource = "partOfRelationNoTagsNoIntersectionAtlas.osm")
    private Atlas atlasPartOfRelationNoTagsNoIntersection;

    public Atlas getNoRelationNoTagsAtIntersectionAtlas()
    {
        return this.atlasWithoutRelationNoTagsAtIntersection;
    }

    public Atlas getNoRelationNoTagsNoIntersectionAtlas()
    {
        return this.atlasWithoutRelationNoTagsNoIntersection;
    }

    public Atlas getNoRelationWithTagsAtIntersectionAtlas()
    {
        return this.atlasWithoutRelationWithTagsAtIntersection;
    }

    public Atlas getNoRelationWithTagsNoIntersectionAtlas()
    {
        return this.atlasWithoutRelationWithTagsNoIntersection;
    }

    public Atlas getPartOfRelationNoTagsAtIntersectionAtlas()
    {
        return this.atlasPartOfRelationNoTagsAtIntersection;
    }

    public Atlas getPartOfRelationNoTagsNoIntersectionAtlas()
    {
        return this.atlasPartOfRelationNoTagsNoIntersection;
    }

    public Atlas getPartOfRelationWithTagsAtIntersectionAtlas()
    {
        return this.atlasPartOfRelationWithTagsAtIntersection;
    }

    public Atlas getPartOfRelationWithTagsNoIntersectionAtlas()
    {
        return this.atlasPartOfRelationWithTagsNoIntersection;
    }
}
