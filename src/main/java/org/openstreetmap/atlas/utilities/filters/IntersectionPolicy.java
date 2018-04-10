package org.openstreetmap.atlas.utilities.filters;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;

/**
 * Interface to be implemented if custom intersection logic is required.
 *
 * @author jklamer
 */
public interface IntersectionPolicy extends Serializable
{

    /**
     * A case by case common sense implementation.
     */
    IntersectionPolicy DEFAULT_INTERSECTION_POLICY = new IntersectionPolicy()
    {
        @Override
        public boolean multiPolygonEntityIntersecting(final MultiPolygon multiPolygon,
                final AtlasEntity entity)
        {
            if (entity instanceof LineItem)
            {
                return multiPolygon.overlaps(((LineItem) entity).asPolyLine());
            }
            if (entity instanceof LocationItem)
            {
                return multiPolygon
                        .fullyGeometricallyEncloses(((LocationItem) entity).getLocation());
            }
            if (entity instanceof Area)
            {
                return multiPolygon.overlaps(((Area) entity).asPolygon());
            }
            if (entity instanceof Relation)
            {
                return ((Relation) entity).members().stream().map(RelationMember::getEntity)
                        .anyMatch(relationEntity -> this
                                .multiPolygonEntityIntersecting(multiPolygon, relationEntity));
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean polygonEntityIntersecting(final Polygon polygon, final AtlasEntity entity)
        {
            return entity.intersects(polygon);
        }

        @Override
        public boolean geometricSurfaceEntityIntersecting(final GeometricSurface geometricSurface,
                final AtlasEntity entity)
        {
            return entity.intersects(geometricSurface);
        }
    };

    default boolean geometricSurfaceEntityIntersecting(GeometricSurface geometricSurface,
            AtlasEntity entity)
    {
        return false;
    }

    default boolean multiPolygonEntityIntersecting(MultiPolygon multiPolygon, AtlasEntity entity)
    {
        return false;
    }

    default boolean polygonEntityIntersecting(Polygon polygon, AtlasEntity entity)
    {
        return false;
    }
}
