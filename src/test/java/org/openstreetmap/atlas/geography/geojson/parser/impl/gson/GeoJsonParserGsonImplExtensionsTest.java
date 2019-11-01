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
public class GeoJsonParserGsonImplExtensionsTest extends AbstractGeoJsonParserGsonImplTest
{
    private static final Logger log = LoggerFactory.getLogger(
            GeoJsonParserGsonImplExtensionsTest.class);
    
    @Test
    public void beanA()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        final BeanA beanA = geoJsonParser.deserializeExtension(json, BeanA.class);
        log.info("beanA:: {}.", beanA);
        
        Assert.assertNotNull(beanA);
        Assert.assertFalse(beanA.getTags().isEmpty());
    }
    
    @Test
    public void beanBWithArray()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
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
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        final BeanB beanB = geoJsonParser.deserializeExtension(json, BeanB.class);
        log.info("beanB:: {}.", beanB);
        
        Assert.assertNotNull(beanB);
        Assert.assertFalse(beanB.getBeanA().getTags().isEmpty());
    }
    
    @Test
    public void description1()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        final Description description = geoJsonParser.deserializeExtension(json, Description.class);
        log.info("description:: {}.", description);
        
        Assert.assertNotNull(description);
        Assert.assertEquals(9, description.getDescriptors().length);
    }
    
    @Test
    public void descriptor1()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);
        
        Assert.assertNotNull(descriptor);
        Assert.assertEquals("ADD", descriptor.getType());
    }
    
    @Test
    public void descriptor2()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        final Descriptor descriptor = geoJsonParser.deserializeExtension(json, Descriptor.class);
        log.info("descriptor:: {}.", descriptor);
        
        Assert.assertNotNull(descriptor);
        Assert.assertEquals("UPDATE", descriptor.getType());
    }
    
    @Test
    public void descriptor3()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
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
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        final FeatureChangeProperties featureChangeProperties = geoJsonParser.deserializeExtension(
                json, FeatureChangeProperties.class);
        log.info("featureChangeProperties:: {}.", featureChangeProperties);
        
        Assert.assertEquals(9, featureChangeProperties.getDescription().getDescriptors().length);
        Assert.assertFalse(featureChangeProperties.getWKT().isEmpty());
        Assert.assertFalse(featureChangeProperties.getBboxWKT().isEmpty());
    }
    
    @Test
    public void featureChangePropertiesBad1()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        try
        {
            final FeatureChangeProperties featureChangeProperties = geoJsonParser.deserializeExtension(
                    json, FeatureChangeProperties.class);
        }
        catch (final Exception e)
        {
            final String message1 = e.getCause().getMessage();
            Assert.assertTrue(message1.startsWith("Population failed. propertyDescriptor name"));
            String message2 = e.getCause().getCause().getCause().getMessage();
            Assert.assertTrue(
                    message2.equals("Can not call newInstance() on the Class for java.lang.Class"));
            return;
        }
        
        Assert.fail("field name `class` should have caused a failure.");
    }
    
    @Test
    public void featureChangePropertiesBad2()
    {
        final String json = extractJsonForExtension();
        
        final GeoJsonParser geoJsonParser = GeoJsonParserGsonImpl.instance;
        
        try
        {
            final FeatureChangeProperties featureChangeProperties = geoJsonParser.deserializeExtension(
                    json, FeatureChangeProperties.class);
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
}
