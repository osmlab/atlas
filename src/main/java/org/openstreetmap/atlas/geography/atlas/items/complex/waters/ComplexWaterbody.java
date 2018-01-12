package org.openstreetmap.atlas.geography.atlas.items.complex.waters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A waterbody usually refers to a area of (standing) water and typically has polygonal geometry.
 * This contrasts with waterways (usually flowing) like streams which typically have linear geometry
 *
 * @author Sid
 */
public class ComplexWaterbody extends ComplexWaterEntity
{
    private static final long serialVersionUID = -666543090371777011L;

    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();

    private static final Logger logger = LoggerFactory.getLogger(ComplexWaterbody.class);

    private MultiPolygon geometry;

    public ComplexWaterbody(final AtlasEntity source, final WaterType type)
    {
        super(source, type);
    }

    public MultiPolygon getGeometry()
    {
        return this.geometry;
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    @Override
    protected void populateGeometry()
    {
        final AtlasEntity source = getSource();
        if (source instanceof Area)
        {
            this.geometry = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(source);
            return;
        }
        else if (source instanceof Relation)
        {
            final Relation relation = (Relation) source;
            final String type = relation.tag(RelationTypeTag.KEY);
            if (RelationTypeTag.MULTIPOLYGON_TYPE.equals(type))
            {
                this.geometry = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(relation);
                return;
            }
        }
        throw new CoreException("Geometry is not set for {}", source);
    }
}
