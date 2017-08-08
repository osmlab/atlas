package org.openstreetmap.atlas.streaming.readers.json.converters;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.utilities.conversion.Converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * Converter that converts a {@link Location} object into its coordinate representation in the Geo
 * Json model: "coordinates" : [longitude, latitude]
 *
 * @author matthieun
 */
public class PointCoordinateConverter implements Converter<Location, JsonArray>
{
    private static final double DATE_LINE_LONGITUDE_WEST = -180.0;
    private static final double DATE_LINE_LONGITUDE_EAST = 180.0;

    @Override
    public JsonArray convert(final Location location)
    {
        final JsonArray coordinates = new JsonArray();
        coordinates.add(new JsonPrimitive(location.getLongitude().asDegrees()));
        coordinates.add(new JsonPrimitive(location.getLatitude().asDegrees()));
        return coordinates;
    }

    public Converter<JsonArray, Location> revert()
    {
        return jsonArray ->
        {
            final double latitude = jsonArray.get(1).getAsDouble();
            double longitude = jsonArray.get(0).getAsDouble();
            if (longitude == DATE_LINE_LONGITUDE_EAST)
            {
                longitude = DATE_LINE_LONGITUDE_WEST;
            }
            return new Location(Latitude.degrees(latitude), Longitude.degrees(longitude));
        };
    }
}
