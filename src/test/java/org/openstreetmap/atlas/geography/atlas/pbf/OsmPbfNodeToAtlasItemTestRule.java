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
    @TestAtlas(loadFromTextResource = "noRelationNoTagsNoIntersectionAtlas.atlas.txt")
    private Atlas atlasWithoutRelationNoTagsNoIntersection;

    @TestAtlas(loadFromTextResource = "partOfRelationNoTagsAtIntersectionAtlas.atlas.txt")
    private Atlas atlasPartOfRelationNoTagsAtIntersection;

    @TestAtlas(loadFromTextResource = "partOfRelationWithTagsNoIntersectionAtlas.atlas.txt")
    private Atlas atlasPartOfRelationWithTagsNoIntersection;

    @TestAtlas(loadFromTextResource = "partOfRelationWithTagsAtIntersectionAtlas.atlas.txt")
    private Atlas atlasPartOfRelationWithTagsAtIntersection;

    @TestAtlas(loadFromTextResource = "noRelationWithTagsNoIntersectionAtlas.atlas.txt")
    private Atlas atlasWithoutRelationWithTagsNoIntersection;

    @TestAtlas(loadFromTextResource = "noRelationNoTagsAtIntersectionAtlas.atlas.txt")
    private Atlas atlasWithoutRelationNoTagsAtIntersection;

    @TestAtlas(loadFromTextResource = "noRelationWithTagsAtIntersectionAtlas.atlas.txt")
    private Atlas atlasWithoutRelationWithTagsAtIntersection;

    @TestAtlas(loadFromTextResource = "partOfRelationNoTagsNoIntersectionAtlas.atlas.txt")
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
