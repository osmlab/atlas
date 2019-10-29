package org.openstreetmap.atlas.geography.geojson.parser.impl.gson;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.geojson.parser.GeoJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change.Description;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change.Descriptor;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change.FeatureChangeProperties;
import org.openstreetmap.atlas.geography.geojson.parser.testdomain.BeanA;
import org.openstreetmap.atlas.geography.geojson.parser.testdomain.BeanB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yazad Khambata
 */
public class GeoJsonParserGsonImplExtensionsTest
{
    private static final Logger log = LoggerFactory
            .getLogger(GeoJsonParserGsonImplExtensionsTest.class);

    @Test
    public void testBeanA()
    {
        final String json = "{\n" + "    id: 10,\n" + "    name: \"Hello\",\n"
                + "    score: 10.5,\n" + "    ids: [1,2,3],\n"
                + "    names: [\"hello\", \"hola\", \"bonjour\"],\n"
                + "    scores: [1.414, 3.14159, 2.718],\n" + "    result: true,\n"
                + "    results: [true, false, true],\n"
                + "    tags: {access: \"private\", foot: \"no\"}\n" + "  }";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final BeanA beanA = geoJsonParser.deserializeExtension(json, BeanA.class);
        log.info("beanA:: {}.", beanA);

        Assert.assertNotNull(beanA);
        Assert.assertFalse(beanA.getTags().isEmpty());
    }

    @Test
    public void testBeanBWithArray()
    {
        final String json = "{\n" + "  name: \"outer\",\n" + "  beanA: {\n" + "    id: 10,\n"
                + "    name: \"Hello\",\n" + "    score: 10.5,\n" + "    ids: [1,2,3],\n"
                + "    names: [\"hello\", \"hola\", \"bonjour\"],\n"
                + "    scores: [1.414, 3.14159, 2.718],\n" + "    result: true,\n"
                + "    results: [true, false, true],\n"
                + "    tags: {access: \"private\", foot: \"no\"}\n" + "  },\n" + "  beanAs: [\n"
                + "    {\n" + "      id: 11,\n" + "      name: \"Hello1\",\n"
                + "      score: 10.51,\n" + "      ids: [11,21,31],\n"
                + "      names: [\"hello1\", \"hola1\", \"bonjour1\"],\n"
                + "      scores: [1.414, 3.14159, 2.718],\n" + "      result: true,\n"
                + "      results: [true, false, true],\n"
                + "      tags: {access1: \"private\", foot1: \"no\"}\n" + "    },\n" + "    {\n"
                + "      id: 12,\n" + "      name: \"Hello2\",\n" + "      score: 10.52,\n"
                + "      ids: [12,22,32],\n" + "      names: [\"hello\", \"hola\", \"bonjour\"],\n"
                + "      scores: [1.414, 3.14159, 2.718],\n" + "      result: true,\n"
                + "      results: [true, false, true],\n"
                + "      tags: {access2: \"private\", foot2: \"no\"}\n" + "    }\n" + "  ]\n" + "}";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final BeanB beanB = geoJsonParser.deserializeExtension(json, BeanB.class);
        log.info("beanB:: {}.", beanB);

        Assert.assertNotNull(beanB);
        Assert.assertFalse(beanB.getBeanA().getTags().isEmpty());
        Assert.assertEquals(2, beanB.getBeanAs().length);
    }

    @Test
    public void testBeanBWithoutArray()
    {
        final String json = "{\n" + "  name: \"outer\",\n" + "  beanA: {\n" + "    id: 10,\n"
                + "    name: \"Hello\",\n" + "    score: 10.5,\n" + "    ids: [1,2,3],\n"
                + "    names: [\"hello\", \"hola\", \"bonjour\"],\n"
                + "    scores: [1.414, 3.14159, 2.718],\n" + "    result: true,\n"
                + "    results: [true, false, true],\n"
                + "    tags: {access: \"private\", foot: \"no\"}\n" + "  }}";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final BeanB beanB = geoJsonParser.deserializeExtension(json, BeanB.class);
        log.info("beanB:: {}.", beanB);

        Assert.assertNotNull(beanB);
        Assert.assertFalse(beanB.getBeanA().getTags().isEmpty());
    }

    @Test
    public void testDescription1()
    {
        final String json = "{\n" + "      \"type\": \"UPDATE\",\n" + "      \"descriptors\": [\n"
                + "        {\n" + "          \"name\": \"TAG\",\n"
                + "          \"type\": \"ADD\",\n" + "          \"key\": \"c\",\n"
                + "          \"value\": \"3\"\n" + "        },\n" + "        {\n"
                + "          \"name\": \"TAG\",\n" + "          \"type\": \"UPDATE\",\n"
                + "          \"key\": \"b\",\n" + "          \"value\": \"2a\",\n"
                + "          \"originalValue\": \"2\"\n" + "        },\n" + "        {\n"
                + "          \"name\": \"TAG\",\n" + "          \"type\": \"REMOVE\",\n"
                + "          \"key\": \"a\",\n" + "          \"value\": \"1\"\n" + "        },\n"
                + "        {\n" + "          \"name\": \"GEOMETRY\",\n"
                + "          \"type\": \"ADD\",\n" + "          \"position\": \"5/5\",\n"
                + "          \"afterView\": \"LINESTRING (-122.028932 37.332451, -122.052138 37.317585, -122.0304871 37.3314171)\"\n"
                + "        },\n" + "        {\n" + "          \"name\": \"GEOMETRY\",\n"
                + "          \"type\": \"REMOVE\",\n" + "          \"position\": \"0/5\",\n"
                + "          \"beforeView\": \"LINESTRING (-122.052138 37.317585, -122.0304871 37.3314171, -122.028932 37.332451)\"\n"
                + "        },\n" + "        {\n" + "          \"name\": \"PARENT_RELATION\",\n"
                + "          \"type\": \"ADD\",\n" + "          \"afterView\": \"3\"\n"
                + "        },\n" + "        {\n" + "          \"name\": \"PARENT_RELATION\",\n"
                + "          \"type\": \"REMOVE\",\n" + "          \"beforeView\": \"1\"\n"
                + "        },\n" + "        {\n" + "          \"name\": \"START_NODE\",\n"
                + "          \"type\": \"UPDATE\",\n" + "          \"beforeView\": \"1\",\n"
                + "          \"afterView\": \"10\"\n" + "        },\n" + "        {\n"
                + "          \"name\": \"END_NODE\",\n" + "          \"type\": \"UPDATE\",\n"
                + "          \"beforeView\": \"2\",\n" + "          \"afterView\": \"20\"\n"
                + "        }\n" + "      ]\n" + "    }";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final Description description = geoJsonParser.deserializeExtension(json, Description.class);
        log.info("description:: {}.", description);

        Assert.assertNotNull(description);
        Assert.assertEquals(9, description.getDescriptors().length);
    }

    @Test
    public void testDescriptor1()
    {
        final String json = "{\n" + "          \"name\": \"TAG\",\n"
                + "          \"type\": \"ADD\",\n" + "          \"key\": \"c\",\n"
                + "          \"value\": \"3\"\n" + "        }";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals("ADD", descriptor.getType());
    }

    @Test
    public void testDescriptor2()
    {
        final String json = "{\n" + "          \"name\": \"START_NODE\",\n"
                + "          \"type\": \"UPDATE\",\n" + "          \"beforeView\": \"1\",\n"
                + "          \"afterView\": \"10\"\n" + "        }";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals("UPDATE", descriptor.getType());
    }

    @Test
    public void testDescriptor3()
    {
        final String json = "{\n" + "          \"name\": \"TAG\",\n"
                + "          \"type\": \"UPDATE\",\n" + "          \"key\": \"b\",\n"
                + "          \"value\": \"2a\",\n" + "          \"originalValue\": \"2\"\n"
                + "        }";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals("b", descriptor.getKey());
        Assert.assertEquals("2a", descriptor.getValue());
    }

    @Test
    public void testFeatureChangeProperties()
    {
        final String json = "{\n" + "  \"featureChangeType\": \"ADD\",\n" + "  \"metadata\": {\n"
                + "    \"somekey1\": \"some value 1\",\n" + "    \"somekey2\": \"some value 2\"\n"
                + "  },\n" + "  \"description\": {\n" + "    \"type\": \"UPDATE\",\n"
                + "    \"descriptors\": [\n" + "      {\n" + "        \"name\": \"TAG\",\n"
                + "        \"type\": \"ADD\",\n" + "        \"key\": \"c\",\n"
                + "        \"value\": \"3\"\n" + "      },\n" + "      {\n"
                + "        \"name\": \"TAG\",\n" + "        \"type\": \"UPDATE\",\n"
                + "        \"key\": \"b\",\n" + "        \"value\": \"2a\",\n"
                + "        \"originalValue\": \"2\"\n" + "      },\n" + "      {\n"
                + "        \"name\": \"TAG\",\n" + "        \"type\": \"REMOVE\",\n"
                + "        \"key\": \"a\",\n" + "        \"value\": \"1\"\n" + "      },\n"
                + "      {\n" + "        \"name\": \"GEOMETRY\",\n" + "        \"type\": \"ADD\",\n"
                + "        \"position\": \"5/5\",\n"
                + "        \"afterView\": \"LINESTRING (-122.028932 37.332451, -122.052138 37.317585, -122.0304871 37.3314171)\"\n"
                + "      },\n" + "      {\n" + "        \"name\": \"GEOMETRY\",\n"
                + "        \"type\": \"REMOVE\",\n" + "        \"position\": \"0/5\",\n"
                + "        \"beforeView\": \"LINESTRING (-122.052138 37.317585, -122.0304871 37.3314171, -122.028932 37.332451)\"\n"
                + "      },\n" + "      {\n" + "        \"name\": \"PARENT_RELATION\",\n"
                + "        \"type\": \"ADD\",\n" + "        \"afterView\": \"3\"\n" + "      },\n"
                + "      {\n" + "        \"name\": \"PARENT_RELATION\",\n"
                + "        \"type\": \"REMOVE\",\n" + "        \"beforeView\": \"1\"\n"
                + "      },\n" + "      {\n" + "        \"name\": \"START_NODE\",\n"
                + "        \"type\": \"UPDATE\",\n" + "        \"beforeView\": \"1\",\n"
                + "        \"afterView\": \"10\"\n" + "      },\n" + "      {\n"
                + "        \"name\": \"END_NODE\",\n" + "        \"type\": \"UPDATE\",\n"
                + "        \"beforeView\": \"2\",\n" + "        \"afterView\": \"20\"\n"
                + "      }\n" + "    ]\n" + "  },\n" + "  \"entityType\": \"EDGE\",\n"
                + "  \"completeEntityClass\": \"org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge\",\n"
                + "  \"identifier\": 123,\n" + "  \"tags\": {\n" + "    \"b\": \"2a\",\n"
                + "    \"c\": \"3\"\n" + "  },\n" + "  \" relations\": [\n" + "    2,\n" + "    3\n"
                + "  ],\n" + "  \"startNode\": 10,\n" + "  \"endNode\": 20,\n"
                + "  \"WKT\": \"LINESTRING (-122.009566 37.33531, -122.031007 37.390535, -122.028932 37.332451, -122.052138 37.317585, -122.0304871 37.3314171)\",\n"
                + "  \"bboxWKT\": \"POLYGON ((-122.052138 37.317585, -122.052138 37.390535, -122.009566 37.390535, -122.009566 37.317585, -122.052138 37.317585))\"\n"
                + "}\n";

        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;

        final FeatureChangeProperties featureChangeProperties = geoJsonParser
                .deserializeExtension(json, FeatureChangeProperties.class);
        log.info("featureChangeProperties:: {}.", featureChangeProperties);

        Assert.assertEquals(9, featureChangeProperties.getDescription().getDescriptors().length);
        Assert.assertFalse(featureChangeProperties.getWKT().isEmpty());
        Assert.assertFalse(featureChangeProperties.getBboxWKT().isEmpty());
    }
}
