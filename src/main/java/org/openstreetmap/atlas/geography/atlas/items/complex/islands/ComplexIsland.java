package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.RelationTypeTag;
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
    private static final JtsMultiPolygonToMultiPolygonConverter MULTIPOLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();

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
        }
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
            final Optional<org.locationtech.jts.geom.MultiPolygon> geom = relation.asMultiPolygon();
            if (RelationTypeTag.MULTIPOLYGON_TYPE.equals(type) && geom.isPresent())
            {
                this.multiPolygon = MULTIPOLYGON_CONVERTER.convert(geom.get());
                return;
            }
        }
        else if (source instanceof Area)
        {
            this.multiPolygon = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(source);
            return;
        }
        throw new CoreException("Geometry is not set for {}", source);
    }
}
