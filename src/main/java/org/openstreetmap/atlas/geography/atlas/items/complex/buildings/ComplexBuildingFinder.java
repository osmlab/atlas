package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * {@link Finder} for {@link ComplexBuilding}s
 *
 * @author matthieun
 */
public class ComplexBuildingFinder implements Finder<ComplexBuilding>
{
    @Override
    public Iterable<ComplexBuilding> find(final Atlas atlas)
    {
        // 1. Get all the simple buildings (Areas) that are not outlines.
        final Iterable<Area> simpleBuildings = atlas
                .areas(area -> isBuilding(area) && isSimple(area));
        final Iterable<ComplexBuilding> simpleEntities = Iterables.translate(simpleBuildings,
                ComplexBuilding::new);
        // 2. Get all the complex buildings
        final Iterable<Relation> complexBuildings = atlas
                .relations(relation -> isBuilding(relation) && isSimple(relation));
        final Iterable<ComplexBuilding> complexEntities = Iterables.translate(complexBuildings,
                ComplexBuilding::new);
        // 3. Combine them in a multi iterable.
        return new MultiIterable<>(simpleEntities, complexEntities);
    }

    private boolean hasChildAreaAsBuilding(final Relation relation)
    {
        for (final RelationMember member : relation.members())
        {
            final AtlasEntity entity = member.getEntity();
            final String role = member.getRole();
            if (entity instanceof Area && RelationTypeTag.MULTIPOLYGON_ROLE_OUTER.equals(role)
                    && isBuilding((Area) entity))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isBuilding(final Area area)
    {
        return BuildingTag.isBuilding(area);
    }

    private boolean isBuilding(final Relation relation)
    {
        final String type = relation.tag(RelationTypeTag.KEY);
        /*
         * If we have a multipolygon relation, then it can be a building if it has the building tag
         * itself, or if one outer member of the relation has a building tag (this one is not
         * recommended in OSM, but many occurrences happen)
         */
        return BuildingTag.KEY.equals(type) || RelationTypeTag.MULTIPOLYGON_TYPE.equals(type)
                && (BuildingTag.isBuilding(relation) || hasChildAreaAsBuilding(relation));
    }

    private boolean isSimple(final AtlasEntity entity)
    {
        for (final Relation relation : entity.relations())
        {
            if (isBuilding(relation))
            {
                return false;
            }
        }
        return true;
    }
}
