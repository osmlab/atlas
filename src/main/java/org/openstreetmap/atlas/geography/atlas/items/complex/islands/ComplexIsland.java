package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In cases of lakes/reservoirs with islands, they are modeled as relations of multi-polygon type.
 * The outer polygons are usually the lakes. The inner polygons are islands. See
 * http://www.openstreetmap.org/relation/2314241
 *
 * @author Sid
 */
public class ComplexIsland extends ComplexEntity
{
    private static final long serialVersionUID = 7840944233946510730L;

    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();

    private static final Logger logger = LoggerFactory.getLogger(ComplexIsland.class);

    private MultiPolygon multiPolygon;

    public ComplexIsland(final AtlasEntity source)
    {
        super(source);
        try
        {
            populateGeometry();
        }
        catch (final Exception e)
        {
            logger.warn("Unable to create complex islands from {}", source, e);
            setInvalidReason("Unable to create complex islands", e);
            return;
        }
    }

    @Override
    public List<ComplexEntityError> getAllInvalidations()
    {
        final List<ComplexEntityError> returnValue = new ArrayList<>();
        if (!isValid())
        {
            returnValue.add(getError().get());
        }
        return returnValue;
    }

    public MultiPolygon getGeometry()
    {
        return this.multiPolygon;
    }

    @Override
    public String toString()
    {
        return "Island : " + getSource();
    }

    private void populateGeometry()
    {
        final AtlasEntity source = getSource();
        if (source instanceof Relation)
        {
            final Relation relation = (Relation) source;
            final String type = relation.tag(RelationTypeTag.KEY);
            if (RelationTypeTag.MULTIPOLYGON_TYPE.equals(type))
            {
                final MultiPolygon multiPolygon = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER
                        .convert(relation);
                // All the inland islands are inner polygons within the outer water boundaries
                final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
                for (final Polygon inner : multiPolygon.inners())
                {
                    outerToInners.put(inner, new ArrayList<Polygon>());
                }
                this.multiPolygon = new MultiPolygon(outerToInners);
                return;
            }
        }
        throw new CoreException("Geometry is not set for {}", source);
    }
}
