package org.openstreetmap.atlas.geography.geojson.parser.impl.gson;

import org.junit.Test;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yazad Khambata
 */
public class GeoJsonParserGsonImplTest
{
    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserGsonImplTest.class);

    @Test
    public void point()
    {
        final String json = "{\n" + "    \"type\": \"Point\", \n"
                + "    \"coordinates\": [30, 10]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void pointWithBbox2D()
    {
        final String json = "{\n" + "    \"bbox\": [-1.0, -2.0, 3.0, 4.0],\n"
                + "    \"type\": \"Point\", \n" + "    \"coordinates\": [30, 10]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void pointWithBbox3D()
    {
        final String json = "{\n" + "    \"bbox\": [-1.0, -2.0, 3.0, 4.0, 5.0, 6.0],\n"
                + "    \"type\": \"Point\", \n" + "    \"coordinates\": [30, 10]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void multiPoint()
    {
        final String json = "{\n" + "    \"type\": \"MultiPoint\", \n"
                + "    \"bbox\": [-1.1, -2.1, 3.1, 4.1, 5.1, 6.1],\n" + "    \"coordinates\": [\n"
                + "        [30, 10], [10, 30], [40, 40]\n" + "    ]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void lineString()
    {
        final String json = "{\n" + "    \"type\": \"LineString\", \n"
                + "    \"bbox\": [-1.1, -2.1, 3.1, 4.1, 5.1, 6.1],\n" + "    \"coordinates\": [\n"
                + "        [30, 10], [10, 30], [40, 40]\n" + "    ]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void multiLineString()
    {
        final String json = "{\n" + "    \"type\": \"MultiLineString\", \n"
                + "    \"bbox\": [-1.1, -2.1, 3.1, 4.1, 5.1, 6.1],\n" + "    \"coordinates\": [\n"
                + "        [[35, 10], [45, 45], [15, 40], [10, 20], [35, 10]], \n"
                + "        [[20, 30], [35, 35], [30, 20], [20, 30]]\n" + "    ]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void polygon()
    {
        final String json = "{\n" + "    \"type\": \"Polygon\", \n"
                + "    \"bbox\": [-1.1, -2.1, 3.1, 4.1, 5.1, 6.1],\n" + "    \"coordinates\": [\n"
                + "        [30, 10], [10, 30], [40, 40]\n" + "    ]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void multiPolygon()
    {
        final String json = "{\n" + "    \"type\": \"MultiPolygon\", \n"
                + "    \"bbox\": [-1.1, -2.1, 3.1, 4.1, 5.1, 6.1],\n" + "    \"coordinates\": [\n"
                + "        [[35, 10], [45, 45], [15, 40], [10, 20], [35, 10]], \n"
                + "        [[20, 30], [35, 35], [30, 20], [20, 30]]\n" + "    ]\n" + "}";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }

    @Test
    public void feature()
    {
        final String json = "{\n" + "  \"type\": \"Feature\",\n" + "  \"bbox\": [\n"
                + "    -122.052138,\n" + "    37.317585,\n" + "    -122.009566,\n"
                + "    37.390535\n" + "  ],\n" + "  \"geometry\": {\n"
                + "    \"type\": \"LineString\",\n" + "    \"coordinates\": [\n" + "      [\n"
                + "        -122.009566,\n" + "        37.33531\n" + "      ],\n" + "      [\n"
                + "        -122.031007,\n" + "        37.390535\n" + "      ],\n" + "      [\n"
                + "        -122.028932,\n" + "        37.332451\n" + "      ],\n" + "      [\n"
                + "        -122.052138,\n" + "        37.317585\n" + "      ],\n" + "      [\n"
                + "        -122.0304871,\n" + "        37.3314171\n" + "      ]\n" + "    ]\n"
                + "  },\n" + "  \"properties\": {}\n" + "}\n";

        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);

        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
}
