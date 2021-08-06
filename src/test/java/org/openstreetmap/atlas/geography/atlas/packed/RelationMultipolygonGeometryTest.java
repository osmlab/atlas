package org.openstreetmap.atlas.geography.atlas.packed;

import java.io.IOException;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class RelationMultipolygonGeometryTest
{
    @Test
    public void testEnhancedAtlas()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);

            final PackedAtlas atlas = (PackedAtlas) new AtlasResourceLoader()
                    .load(new File("/Users/foo/test.atlas", filesystem));
            Assert.assertTrue(atlas.containsEnhancedRelationGeometry());
            Assert.assertNotNull(atlas.enhancedRelationGeometries());

            final PackedAtlas atlasNoEnhancedGeometry = (PackedAtlas) new AtlasResourceLoader()
                    .load(new File("/Users/foo/test_notEnhanced.atlas", filesystem));
            Assert.assertFalse(atlasNoEnhancedGeometry.containsEnhancedRelationGeometry());
            Assert.assertNull(atlasNoEnhancedGeometry.enhancedRelationGeometries());

            final PackedAtlas atlasJava = (PackedAtlas) new AtlasResourceLoader()
                    .load(new File("/Users/foo/test_java.atlas", filesystem));
            Assert.assertFalse(atlasJava.containsEnhancedRelationGeometry());
            Assert.assertNull(atlasJava.enhancedRelationGeometries());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testReadingGeometry()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem(filesystem);
            final PackedAtlas atlas = (PackedAtlas) new AtlasResourceLoader()
                    .load(new File("/Users/foo/test.atlas", filesystem));
            Assert.assertTrue(atlas.relation(1L).asMultiPolygon().isPresent());
            final PackedAtlas atlasNoEnhancedGeometry = (PackedAtlas) new AtlasResourceLoader()
                    .load(new File("/Users/foo/test_notEnhanced.atlas", filesystem));
            Assert.assertFalse(atlasNoEnhancedGeometry.relation(1L).asMultiPolygon().isPresent());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem(final FileSystem filesystem)
    {
        /*
         * test.atlas contains a multipolygon with enhanced geometry (storing the JTS geometry
         * directly).
         */
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withEnhancedRelationGeometry();
        builder.addArea(1L, Polygon.SILICON_VALLEY, Maps.hashMap("name", "Silicon Valley"));
        builder.addArea(2L, Polygon.wkt(
                "POLYGON((-122.0416867 37.35,-122.0194565 37.35,-122.0194565 37.3426160,-122.0416867 37.3426160,-122.0416867 37.35))"),
                Maps.hashMap("name", "Silicon Valley Inner"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L, "outer", ItemType.AREA);
        bean.addItem(2L, "inner", ItemType.AREA);
        final MultiMap<Polygon, Polygon> outersToInners = new MultiMap<>();
        outersToInners.add(builder.peek().area(1L).asPolygon(),
                builder.peek().area(2L).asPolygon());
        builder.addRelation(1L, 1L, bean, Maps.hashMap("type", "multipolygon"),
                new JtsMultiPolygonToMultiPolygonConverter()
                        .backwardConvert(new MultiPolygon(outersToInners)));
        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas", filesystem);
        assert atlas != null;
        atlas.save(atlasFile);

        /*
         * test_notEnhanced.atlas contains a multipolygon, but without the special enhancement
         * (storing the JTS geometry directly).
         */
        final PackedAtlasBuilder builder2 = new PackedAtlasBuilder();
        builder2.addArea(1L, Polygon.SILICON_VALLEY, Maps.hashMap("name", "Silicon Valley"));
        builder2.addArea(2L, Polygon.wkt(
                "POLYGON((-122.0416867 37.35,-122.0194565 37.35,-122.0194565 37.3426160,-122.0416867 37.3426160,-122.0416867 37.35))"),
                Maps.hashMap("name", "Silicon Valley Inner"));
        final RelationBean bean2 = new RelationBean();
        bean2.addItem(1L, "outer", ItemType.AREA);
        bean2.addItem(2L, "inner", ItemType.AREA);
        builder2.addRelation(1L, 1L, bean2, Maps.hashMap("type", "multipolygon"));
        final Atlas atlas2 = builder2.get();
        final File atlasFile2 = new File("/Users/foo/test_notEnhanced.atlas", filesystem);
        assert atlas2 != null;
        atlas2.save(atlasFile2);

        /*
         * test_java.atlas contains a multipolygon, but without the special enhancement because it
         * is a legacy Java serialized atlas.
         */
        final PackedAtlasBuilder builder3 = new PackedAtlasBuilder();
        builder3.addArea(1L, Polygon.SILICON_VALLEY, Maps.hashMap("name", "Silicon Valley"));
        builder3.addArea(2L, Polygon.wkt(
                "POLYGON((-122.0416867 37.35,-122.0194565 37.35,-122.0194565 37.3426160,-122.0416867 37.3426160,-122.0416867 37.35))"),
                Maps.hashMap("name", "Silicon Valley Inner"));
        final RelationBean bean3 = new RelationBean();
        bean3.addItem(1L, "outer", ItemType.AREA);
        bean3.addItem(2L, "inner", ItemType.AREA);
        builder3.addRelation(1L, 1L, bean3, Maps.hashMap("type", "multipolygon"));
        final Atlas atlas3 = builder3.get();
        final File atlasFile3 = new File("/Users/foo/test_java.atlas", filesystem);
        assert atlas3 != null;
        atlas3.save(atlasFile3);
    }
}
