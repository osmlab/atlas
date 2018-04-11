package org.openstreetmap.atlas.geography.clipping;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * @author matthieun
 */
public class MultiPolygonClipperTest
{
    private static final MultiPolygon CLIPPING = MultiPolygon.TEST_MULTI_POLYGON;
    private static final MultiPolygon SUBJECT_MULTIPOLYGON;

    static
    {
        final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
        final Polygon outer = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_17,
                Location.forString("37.363819,-121.928993"));
        final Polygon inner = new Polygon(Location.forString("37.320691,-122.023155"),
                Location.forString("37.317882,-122.013252"),
                Location.forString("37.310775,-122.024402"));
        outerToInners.add(outer, inner);
        SUBJECT_MULTIPOLYGON = new MultiPolygon(outerToInners);
    }

    private static final Polygon SUBJECT = new Polygon(Location.forString("37.329869,-122.054441"),
            Location.forString("37.331287,-121.995459"), Location.TEST_3);
    private static final PolyLine SUBJECT_POLYLINE = new PolyLine(
            Location.forString("37.329869,-122.054441"),
            Location.forString("37.331287,-121.995459"), Location.TEST_3);

    @Test
    public void testEmptyClip()
    {
        final Polygon outer = Polygon.wkt(
                "POLYGON ((-122.05576 37.332439,  -122.009566 37.36531, -122.031007 37.390535, -122.05576 37.332439))");
        final MultiPolygon clipping = MultiPolygon.forPolygon(outer);
        final MultiPolygon clipped = SUBJECT_MULTIPOLYGON.clip(clipping, ClipType.AND)
                .getClipMultiPolygon();
        Assert.assertTrue(clipped.isEmpty());
    }

    @Test
    public void testMultiPolygonAnd()
    {
        final MultiPolygon clipped = SUBJECT_MULTIPOLYGON.clip(CLIPPING, ClipType.AND)
                .getClipMultiPolygon();
        Assert.assertEquals(1, clipped.outers().size());
        Assert.assertEquals(2, clipped.inners().size());
    }

    @Test
    public void testMultiPolygonNot()
    {
        final MultiPolygon clipped = SUBJECT_MULTIPOLYGON.clip(CLIPPING, ClipType.NOT)
                .getClipMultiPolygon();
        Assert.assertEquals(2, clipped.outers().size());
        Assert.assertEquals(0, clipped.inners().size());
    }

    @Test
    public void testMultiPolygonOr()
    {
        final MultiPolygon clipped = SUBJECT_MULTIPOLYGON.clip(CLIPPING, ClipType.OR)
                .getClipMultiPolygon();
        Assert.assertEquals(1, clipped.outers().size());
        Assert.assertEquals(0, clipped.inners().size());
    }

    @Test
    public void testMultiPolygonXor()
    {
        final MultiPolygon clipped = SUBJECT_MULTIPOLYGON.clip(CLIPPING, ClipType.XOR)
                .getClipMultiPolygon();
        Assert.assertEquals(4, clipped.outers().size());
        Assert.assertEquals(0, clipped.inners().size());
    }

    @Test
    public void testPolygonAnd()
    {
        final MultiPolygon clipped = SUBJECT.clip(CLIPPING, ClipType.AND).getClipMultiPolygon();
        Assert.assertEquals(2, clipped.outers().size());
        Assert.assertEquals(0, clipped.inners().size());
    }

    @Test
    public void testPolygonNot()
    {
        final MultiPolygon clipped = SUBJECT.clip(CLIPPING, ClipType.NOT).getClipMultiPolygon();
        Assert.assertEquals(3, clipped.outers().size());
        Assert.assertEquals(0, clipped.inners().size());
    }

    @Test
    public void testPolygonOr()
    {
        final MultiPolygon clipped = SUBJECT.clip(CLIPPING, ClipType.OR).getClipMultiPolygon();
        Assert.assertEquals(1, clipped.outers().size());
        Assert.assertEquals(2, clipped.inners().size());
    }

    @Test
    public void testPolygonXor()
    {
        final MultiPolygon clipped = SUBJECT.clip(CLIPPING, ClipType.XOR).getClipMultiPolygon();
        Assert.assertEquals(5, clipped.outers().size());
        Assert.assertEquals(0, clipped.inners().size());
    }

    @Test
    public void testPolyLineAnd()
    {
        final List<? extends PolyLine> clipped = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.AND)
                .getClip();
        Assert.assertEquals(4, clipped.size());
    }

    @Test
    public void testPolyLineNot()
    {
        final List<? extends PolyLine> clipped = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.NOT)
                .getClip();
        Assert.assertEquals(5, clipped.size());
    }

    @Test
    public void testPolyLineOr()
    {
        final List<? extends PolyLine> clipped = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.OR)
                .getClip();
        Assert.assertEquals(5, clipped.size());
    }

    @Test
    public void testPolyLineXor()
    {
        final List<? extends PolyLine> clipped = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.XOR)
                .getClip();
        Assert.assertEquals(5, clipped.size());
    }
}
