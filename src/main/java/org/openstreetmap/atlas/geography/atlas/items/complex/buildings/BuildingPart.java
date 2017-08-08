package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Part of a {@link ComplexBuilding}. Can be a simple {@link Area}, or a {@link MultiPolygon}
 * {@link Relation} that can be a building part with holes
 *
 * @author matthieun
 */
public class BuildingPart extends ComplexEntity
{
    private static final long serialVersionUID = 364620404649236692L;

    private static final Logger logger = LoggerFactory.getLogger(BuildingPart.class);
    private static final RelationOrAreaToMultiPolygonConverter BUILDING_OUTLINE_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final HeightConverter HEIGHT_CONVERTER = new HeightConverter();

    private MultiPolygon geometry;

    public BuildingPart(final AtlasEntity source)
    {
        super(source);
        try
        {
            this.geometry = BUILDING_OUTLINE_CONVERTER.convert(source);
        }
        catch (final Exception e)
        {
            logger.warn("Unable to create building part from {}", source, e);
            setInvalidReason("Unable to create building part", e);
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
        return this.geometry;
    }

    /**
     * @return The building part's top height
     */
    public Optional<Distance> topHeight()
    {
        final Map<String, String> tags = getSource().getTags();
        final String heightTag = tags.get("height");
        try
        {

            if (heightTag != null)
            {
                return Optional.of(HEIGHT_CONVERTER.convert(heightTag));
            }
        }
        catch (final Exception e)
        {
            logger.warn("Invalid height {} for building part id {}", heightTag,
                    getSource().getIdentifier());
        }
        return Optional.empty();
    }

    @Override
    public String toString()
    {
        return "[BuildingPart: Geometry = " + this.geometry.toReadableString() + "]";
    }
}
