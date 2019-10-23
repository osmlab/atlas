package org.openstreetmap.atlas.geography.geojson.parser.impl;

import com.google.gson.Gson;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Point;

/**
 * @author Yazad Khambata
 */
public class GeoJsonParserGSONImpl implements GoeJsonParser {
    @Override
    public GeoJsonItem deserialize(final String geoJson) {
        final Gson gson = new Gson();
        final Point point = gson.fromJson(geoJson, Point.class);

        return point;
    }

    public static void main(String[] args) {
        final GoeJsonParser goeJsonParser = new GeoJsonParserGSONImpl();
        final String json = "{\n" +
                "    \"type\": \"Point\", \n" +
                "    \"coordinates\": [30, 10]\n" +
                "}";

        final GeoJsonItem geoJsonItem = goeJsonParser.deserialize(json);

        System.out.println(geoJsonItem);
    }
}
