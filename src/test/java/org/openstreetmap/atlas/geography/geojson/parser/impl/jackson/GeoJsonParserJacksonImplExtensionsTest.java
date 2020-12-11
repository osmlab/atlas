package org.openstreetmap.atlas.geography.geojson.parser.impl.jackson;

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
public class GeoJsonParserJacksonImplExtensionsTest extends AbstractGeoJsonParserGsonImplTest
{
    private static final Logger log = LoggerFactory
            .getLogger(GeoJsonParserJacksonImplExtensionsTest.class);

    @Test
    public void beanA()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final BeanA beanA = geoJsonParser.deserializeExtension(json, BeanA.class);
        log.info("beanA:: {}.", beanA);

        Assert.assertNotNull(beanA);
        Assert.assertFalse(beanA.getTags().isEmpty());
    }

    @Test
    public void beanBWithArray()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final BeanB beanB = geoJsonParser.deserializeExtension(json, BeanB.class);
        log.info("beanB:: {}.", beanB);

        Assert.assertNotNull(beanB);
        Assert.assertFalse(beanB.getBeanA().getTags().isEmpty());
        Assert.assertEquals(2, beanB.getBeanAs().length);
    }

    @Test
    public void beanBWithoutArray()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final BeanB beanB = geoJsonParser.deserializeExtension(json, BeanB.class);
        log.info("beanB:: {}.", beanB);

        Assert.assertNotNull(beanB);
        Assert.assertFalse(beanB.getBeanA().getTags().isEmpty());
    }

    @Test
    public void description1()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final Description description = geoJsonParser.deserializeExtension(json, Description.class);
        log.info("description:: {}.", description);

        Assert.assertNotNull(description);
        Assert.assertEquals(9, description.getDescriptors().length);
    }

    @Test
    public void descriptor1()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals("ADD", descriptor.getType());
    }

    @Test
    public void descriptor2()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals("UPDATE", descriptor.getType());
    }

    @Test
    public void descriptor3()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals("b", descriptor.getKey());
        Assert.assertEquals("2a", descriptor.getValue());
    }

    @Test
    public void featureChangeProperties()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final FeatureChangeProperties featureChangeProperties = geoJsonParser
                .deserializeExtension(json, FeatureChangeProperties.class);
        log.info("featureChangeProperties:: {}.", featureChangeProperties);

        Assert.assertEquals(9, featureChangeProperties.getDescription().getDescriptors().length);
        Assert.assertFalse(featureChangeProperties.getWKT().isEmpty());
        Assert.assertFalse(featureChangeProperties.getBboxWKT().isEmpty());
    }

    @Test
    public void featureChangePropertiesBad1()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        try
        {
            final FeatureChangeProperties featureChangeProperties = geoJsonParser
                    .deserializeExtension(json, FeatureChangeProperties.class);
        }
        catch (final Exception e)
        {
            final String message1 = e.getCause().getMessage();
            Assert.assertTrue(message1.startsWith("Population failed. propertyDescriptor name"));
            final String message2 = e.getCause().getCause().getCause().getMessage();
            Assert.assertTrue(
                    "Can not call newInstance() on the Class for java.lang.Class".equals(message2));
            return;
        }

        Assert.fail("field name `class` should have caused a failure.");
    }

    @Test
    public void featureChangePropertiesBad2()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        try
        {
            final FeatureChangeProperties featureChangeProperties = geoJsonParser
                    .deserializeExtension(json, FeatureChangeProperties.class);
        }
        catch (final Exception e)
        {
            final String message1 = e.getCause().getMessage();
            Assert.assertTrue(
                    message1.startsWith("Population failed. propertyDescriptor name: relations"));
            Assert.assertEquals(ClassCastException.class, e.getCause().getCause().getClass());
            return;
        }

        Assert.fail("data data value should have caused a failure.");
    }

    @Test
    public void featureChangePropertiesRelationMemberDescriptor()
    {
        final String json = extractJsonForExtension();

        final GeoJsonParser geoJsonParser = GeoJsonParserJacksonImpl.instance;

        final FeatureChangeProperties featureChangeProperties = geoJsonParser
                .deserializeExtension(json, FeatureChangeProperties.class);
        log.info("featureChangeProperties:: {}.", featureChangeProperties);

        final Descriptor descriptor = featureChangeProperties.getDescription().getDescriptors()[0];

        Assert.assertEquals(402306209000000L, (long) descriptor.getId());
        Assert.assertEquals("NODE", descriptor.getItemType());
        Assert.assertEquals("via", descriptor.getRole());
    }
}
