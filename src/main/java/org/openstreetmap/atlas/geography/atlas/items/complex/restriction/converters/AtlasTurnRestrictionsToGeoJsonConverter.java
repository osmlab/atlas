package org.openstreetmap.atlas.geography.atlas.items.complex.restriction.converters;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.restriction.ComplexTurnRestrictionFinder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * @author matthieun
 */
public class AtlasTurnRestrictionsToGeoJsonConverter implements Converter<Atlas, GeoJsonObject>
{
    @Override
    public GeoJsonObject convert(final Atlas atlas)
    {
        return new GeoJsonBuilder()
                .create(Iterables.translate(new ComplexTurnRestrictionFinder().find(atlas),
                        turnRestriction -> turnRestriction.getTurnRestriction().asGeoJson()));
    }
}
