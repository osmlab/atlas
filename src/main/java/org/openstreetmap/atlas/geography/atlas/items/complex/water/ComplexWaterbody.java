package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
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

    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter(
            true);
    private static final JtsMultiPolygonToMultiPolygonConverter MULTIPOLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();

    private static final Logger logger = LoggerFactory.getLogger(ComplexWaterbody.class);

    private MultiPolygon geometry;

    public ComplexWaterbody(final AtlasEntity source, final WaterType type)
    {
        super(source, type);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof ComplexWaterbody)
        {
            final ComplexWaterbody that = (ComplexWaterbody) other;
            return new EqualsBuilder().append(this.getWaterType(), that.getWaterType())
                    .append(this.getSource(), that.getSource())
                    .append(this.getGeometry().toWkt(), that.getGeometry().toWkt()).build();
        }
        return false;
    }

    public MultiPolygon getGeometry()
    {
        return this.geometry;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.getSource()).append(this.getWaterType())
                .append(this.getGeometry().toWkb()).build();
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
            if (RelationTypeTag.MULTIPOLYGON_TYPE.equals(type)
                    && relation.asMultiPolygon().isPresent())
            {
                this.geometry = MULTIPOLYGON_CONVERTER.convert(
                        (org.locationtech.jts.geom.MultiPolygon) relation.asMultiPolygon().get());
                return;
            }
        }
        throw new CoreException("Geometry is not set for {}", source);
    }
}
