package org.openstreetmap.atlas.geography.geojson.parser.impl.gson;

import org.junit.Test;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change.FeatureChangeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some of the examples are taken and modified from wikipedia and geojson spec.
 *
 * @author Yazad Khambata
 */
public class GeoJsonParserGsonImplTest
{
    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserGsonImplTest.class);
    
    @Test
    public void feature1()
    {
        final String json =
                "{\n" + "  \"type\": \"Feature\",\n" + "  \"bbox\": [\n" + "    -122.052138,\n"
                        + "    37.317585,\n" + "    -122.009566,\n" + "    37.390535\n" + "  ],\n"
                        + "  \"geometry\": {\n" + "    \"type\": \"LineString\",\n"
                        + "    \"coordinates\": [\n" + "      [\n" + "        -122.009566,\n"
                        + "        37.33531\n" + "      ],\n" + "      [\n"
                        + "        -122.031007,\n" + "        37.390535\n" + "      ],\n"
                        + "      [\n" + "        -122.028932,\n" + "        37.332451\n"
                        + "      ],\n" + "      [\n" + "        -122.052138,\n"
                        + "        37.317585\n" + "      ],\n" + "      [\n"
                        + "        -122.0304871,\n" + "        37.3314171\n" + "      ]\n"
                        + "    ]\n" + "  },\n" + "  \"properties\": {}\n" + "}\n";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void feature2()
    {
        final String json = "{\n" + "      \"type\": \"Feature\",\n" + "      \"geometry\": {\n"
                + "        \"type\": \"Polygon\",\n" + "        \"coordinates\": \n"
                + "          [[\n" + "            [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n"
                + "            [100.0, 1.0], [100.0, 0.0]\n" + "          ]]\n" + "        \n"
                + "      },\n" + "      \"properties\": {\n" + "        \"prop0\": \"value0\",\n"
                + "        \"prop1\": { \"this\": \"that\" }\n" + "      }\n" + "    }";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void featureCollection1()
    {
        final String json =
                "{\n" + "  \"type\": \"FeatureCollection\",\n" + "  \"features\": [\n" + "    {\n"
                        + "      \"type\": \"Feature\",\n" + "      \"geometry\": {\n"
                        + "        \"type\": \"Point\",\n"
                        + "        \"coordinates\": [102.0, 0.5]\n" + "      },\n"
                        + "      \"properties\": {\n" + "        \"prop0\": \"value0\"\n"
                        + "      }\n" + "    },\n" + "    {\n" + "      \"type\": \"Feature\",\n"
                        + "      \"geometry\": {\n" + "        \"type\": \"LineString\",\n"
                        + "        \"coordinates\": [\n"
                        + "          [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n"
                        + "        ]\n" + "      },\n" + "      \"properties\": {\n"
                        + "        \"prop0\": \"value0\",\n" + "        \"prop1\": 0.0\n"
                        + "      }\n" + "    },\n" + "    {\n" + "      \"type\": \"Feature\",\n"
                        + "      \"geometry\": {\n" + "        \"type\": \"Polygon\",\n"
                        + "        \"coordinates\": [\n" + "          [\n"
                        + "            [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n"
                        + "            [100.0, 1.0], [100.0, 0.0]\n" + "          ]\n"
                        + "        ]\n" + "      },\n" + "      \"properties\": {\n"
                        + "        \"prop0\": \"value0\",\n"
                        + "        \"prop1\": { \"this\": \"that\" }\n" + "      }\n" + "    }\n"
                        + "  ]\n" + "}";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void featureWithExtendedProperties()
    {
        final String json =
                "{\n" + "  \"type\": \"Feature\",\n" + "  \"bbox\": [\n" + "    -122.052138,\n"
                        + "    37.317585,\n" + "    -122.009566,\n" + "    37.390535\n" + "  ],\n"
                        + "  \"geometry\": {\n" + "    \"type\": \"LineString\",\n"
                        + "    \"coordinates\": [\n" + "      [\n" + "        -122.009566,\n"
                        + "        37.33531\n" + "      ],\n" + "      [\n"
                        + "        -122.031007,\n" + "        37.390535\n" + "      ],\n"
                        + "      [\n" + "        -122.028932,\n" + "        37.332451\n"
                        + "      ],\n" + "      [\n" + "        -122.052138,\n"
                        + "        37.317585\n" + "      ],\n" + "      [\n"
                        + "        -122.0304871,\n" + "        37.3314171\n" + "      ]\n"
                        + "    ]\n" + "  },\n" + "  \"properties\": {\n"
                        + "    \"featureChangeType\": \"ADD\",\n" + "    \"metadata\": {\n"
                        + "      \"somekey1\": \"some value 1\",\n"
                        + "      \"somekey2\": \"some value 2\"\n" + "    },\n"
                        + "    \"description\": {\n" + "      \"type\": \"UPDATE\",\n"
                        + "      \"descriptors\": [\n" + "        {\n"
                        + "          \"name\": \"TAG\",\n" + "          \"type\": \"ADD\",\n"
                        + "          \"key\": \"c\",\n" + "          \"value\": \"3\"\n"
                        + "        },\n" + "        {\n" + "          \"name\": \"TAG\",\n"
                        + "          \"type\": \"UPDATE\",\n" + "          \"key\": \"b\",\n"
                        + "          \"value\": \"2a\",\n" + "          \"originalValue\": \"2\"\n"
                        + "        },\n" + "        {\n" + "          \"name\": \"TAG\",\n"
                        + "          \"type\": \"REMOVE\",\n" + "          \"key\": \"a\",\n"
                        + "          \"value\": \"1\"\n" + "        },\n" + "        {\n"
                        + "          \"name\": \"GEOMETRY\",\n" + "          \"type\": \"ADD\",\n"
                        + "          \"position\": \"5/5\",\n"
                        + "          \"afterView\": \"LINESTRING (-122.028932 37.332451, -122.052138 37.317585, -122.0304871 37.3314171)\"\n"
                        + "        },\n" + "        {\n" + "          \"name\": \"GEOMETRY\",\n"
                        + "          \"type\": \"REMOVE\",\n" + "          \"position\": \"0/5\",\n"
                        + "          \"beforeView\": \"LINESTRING (-122.052138 37.317585, -122.0304871 37.3314171, -122.028932 37.332451)\"\n"
                        + "        },\n" + "        {\n"
                        + "          \"name\": \"PARENT_RELATION\",\n"
                        + "          \"type\": \"ADD\",\n" + "          \"afterView\": \"3\"\n"
                        + "        },\n" + "        {\n"
                        + "          \"name\": \"PARENT_RELATION\",\n"
                        + "          \"type\": \"REMOVE\",\n" + "          \"beforeView\": \"1\"\n"
                        + "        },\n" + "        {\n" + "          \"name\": \"START_NODE\",\n"
                        + "          \"type\": \"UPDATE\",\n" + "          \"beforeView\": \"1\",\n"
                        + "          \"afterView\": \"10\"\n" + "        },\n" + "        {\n"
                        + "          \"name\": \"END_NODE\",\n"
                        + "          \"type\": \"UPDATE\",\n" + "          \"beforeView\": \"2\",\n"
                        + "          \"afterView\": \"20\"\n" + "        }\n" + "      ]\n"
                        + "    },\n" + "    \"entityType\": \"EDGE\",\n"
                        + "    \"completeEntityClass\": \"org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge\",\n"
                        + "    \"identifier\": 123,\n" + "    \"tags\": {\n"
                        + "      \"b\": \"2a\",\n" + "      \"c\": \"3\"\n" + "    },\n"
                        + "    \" relations\": [\n" + "      2,\n" + "      3\n" + "    ],\n"
                        + "    \"startNode\": 10,\n" + "    \"endNode\": 20,\n"
                        + "    \"WKT\": \"LINESTRING (-122.009566 37.33531, -122.031007 37.390535, -122.028932 37.332451, -122.052138 37.317585, -122.0304871 37.3314171)\",\n"
                        + "    \"bboxWKT\": \"POLYGON ((-122.052138 37.317585, -122.052138 37.390535, -122.009566 37.390535, -122.009566 37.317585, -122.052138 37.317585))\"\n"
                        + "  }\n" + "}\n";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
        
        final FeatureChangeProperties featureChangeProperties = geoJsonItem.getProperties().asType(
                FeatureChangeProperties.class);
        
        log.info("featureChangeProperties:: {}.", featureChangeProperties);
    }
    
    @Test
    public void foreignFieldsNested()
    {
        final String json = "{\n" + "    \"type\": \"Point\", \n"
                + "'unknown': {'unknown_key1': 'unknown_value1','unknown_key2': 'unknown_value2'},"
                + "    \"coordinates\": [30, 10]\n" + "}";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void foreignFieldsSimple()
    {
        final String json = "{\n" + "    \"type\": \"Point\", \n"
                + "'unknown_key1': 'unknown_value1','unknown_key2': 'unknown_value2',"
                + "    \"coordinates\": [30, 10]\n" + "}";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void geometryCollectionBasic()
    {
        final String json =
                "{\n" + "    \"type\": \"GeometryCollection\",\n" + "    \"geometries\": [\n"
                        + "        {\n" + "            \"type\": \"Point\",\n"
                        + "            \"coordinates\": [40, 10]\n" + "        },\n" + "        {\n"
                        + "            \"type\": \"LineString\",\n"
                        + "            \"coordinates\": [\n"
                        + "                [10, 10], [20, 20], [10, 40]\n" + "            ]\n"
                        + "        },\n" + "        {\n" + "            \"type\": \"Polygon\",\n"
                        + "            \"coordinates\": [\n"
                        + "                [[40, 40], [20, 45], [45, 30], [40, 40]]\n"
                        + "            ]\n" + "        }\n" + "    ]\n" + "}\n";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void geometryCollectionRecursiveNested()
    {
        final String json =
                "{\n" + "    \"type\": \"GeometryCollection\",\n" + "    \"geometries\": [\n"
                        + "        {\n" + "            \"type\": \"Point\",\n"
                        + "            \"coordinates\": [40, 10]\n" + "        },\n" + "        {\n"
                        + "            \"type\": \"LineString\",\n"
                        + "            \"coordinates\": [\n"
                        + "                [10, 10], [20, 20], [10, 40]\n" + "            ]\n"
                        + "        },\n" + "        {\n" + "            \"type\": \"Polygon\",\n"
                        + "            \"coordinates\": [\n"
                        + "                [[40, 40], [20, 45], [45, 30], [40, 40]]\n"
                        + "            ]\n" + "        },\n" + "        {\n"
                        + "            \"type\": \"GeometryCollection\",\n"
                        + "            \"geometries\": [\n" + "                {\n"
                        + "                    \"type\": \"Point\",\n"
                        + "                    \"coordinates\": [40, 10]\n" + "                },\n"
                        + "                {\n" + "                    \"type\": \"LineString\",\n"
                        + "                    \"coordinates\": [\n"
                        + "                        [10, 10], [20, 20], [10, 40]\n"
                        + "                    ]\n" + "                },\n" + "                {\n"
                        + "                    \"type\": \"Polygon\",\n"
                        + "                    \"coordinates\": [\n"
                        + "                        [[40, 40], [20, 45], [45, 30], [40, 40]]\n"
                        + "                    ]\n" + "                },\n" + "                {\n"
                        + "                    \"type\": \"GeometryCollection\",\n"
                        + "                    \"geometries\": [\n" + "                        {\n"
                        + "                            \"type\": \"Point\",\n"
                        + "                            \"coordinates\": [40, 10]\n"
                        + "                        },\n" + "                        {\n"
                        + "                            \"type\": \"LineString\",\n"
                        + "                            \"coordinates\": [\n"
                        + "                                [10, 10], [20, 20], [10, 40]\n"
                        + "                            ]\n" + "                        },\n"
                        + "                        {\n"
                        + "                            \"type\": \"Polygon\",\n"
                        + "                            \"coordinates\": [\n"
                        + "                                [[40, 40], [20, 45], [45, 30], [40, 40]]\n"
                        + "                            ]\n" + "                        },\n"
                        + "                        {\n"
                        + "                            \"type\": \"GeometryCollection\",\n"
                        + "                            \"geometries\": [\n"
                        + "                                {\n"
                        + "                                    \"type\": \"Point\",\n"
                        + "                                    \"coordinates\": [40, 10]\n"
                        + "                                },\n"
                        + "                                {\n"
                        + "                                    \"type\": \"LineString\",\n"
                        + "                                    \"coordinates\": [\n"
                        + "                                        [10, 10], [20, 20], [10, 40]\n"
                        + "                                    ]\n"
                        + "                                },\n"
                        + "                                {\n"
                        + "                                    \"type\": \"Polygon\",\n"
                        + "                                    \"coordinates\": [\n"
                        + "                                        [[40, 40], [20, 45], [45, 30], [40, 40]]\n"
                        + "                                    ]\n"
                        + "                                }\n" + "                            ]\n"
                        + "                        }\n" + "                    ]\n"
                        + "                }\n" + "            ]\n" + "        }\n" + "    ]\n"
                        + "}\n";
        
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
    public void multiPoint()
    {
        final String json = "{\n" + "    \"type\": \"MultiPoint\", \n"
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
    public void point()
    {
        final String json =
                "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [30, 10]\n" + "}";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void pointWithBbox2D()
    {
        final String json =
                "{\n" + "    \"bbox\": [-1.0, -2.0, 3.0, 4.0],\n" + "    \"type\": \"Point\", \n"
                        + "    \"coordinates\": [30, 10]\n" + "}";
        
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
    public void polygon()
    {
        final String json = "{\n" + "    \"type\": \"Polygon\", \n" + "    \"coordinates\": [\n"
                + "        [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]\n" + "    ]\n" + "}";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void propertiesNested()
    {
        final String json = "        {\n" + "            \"type\": \"Point\",\n"
                + "            \"coordinates\": [102.0, 0.5],\n" + "            \"properties\": {\n"
                + "                \"prop0\": {'a': 123, 'b': 'hello', 'c': 10.5},\n"
                + "                \"prop1\": \"value1\"\n" + "            }\n" + "        }";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
    
    @Test
    public void propertiesSimple()
    {
        final String json = "        {\n" + "            \"type\": \"Point\",\n"
                + "            \"coordinates\": [102.0, 0.5],\n" + "            \"properties\": {\n"
                + "                \"prop0\": \"value0\",\n"
                + "                \"prop1\": \"value1\"\n" + "            }\n" + "        }";
        
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
        
        log.info("geoJsonItem:: {}.", geoJsonItem);
    }
}
