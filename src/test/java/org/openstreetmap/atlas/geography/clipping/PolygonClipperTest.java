package org.openstreetmap.atlas.geography.clipping;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;

/**
 * @author matthieun
 */
public class PolygonClipperTest
{
    private static final Polygon CLIPPING = Polygon.SILICON_VALLEY_2;
    private static final Polygon SUBJECT = new Polygon(Location.CROSSING_85_280, Location.TEST_6,
            Location.TEST_4, Location.STEVENS_CREEK, Location.CROSSING_85_17);
    private static final PolyLine SUBJECT_POLYLINE = new PolyLine(Location.CROSSING_85_280,
            Location.TEST_6, Location.TEST_4, Location.STEVENS_CREEK, Location.CROSSING_85_17);

    @Test
    public void testPolyLineAnd()
    {
        final List<? extends PolyLine> clips = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.AND)
                .getClip();
        Assert.assertEquals(2, clips.size());
    }

    @Test
    public void testPolyLineNot()
    {
        final List<? extends PolyLine> clips = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.NOT)
                .getClip();
        Assert.assertEquals(3, clips.size());
    }

    @Test
    public void testPolyLineOr()
    {
        final List<? extends PolyLine> clips = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.OR)
                .getClip();
        Assert.assertEquals(4, clips.size());
    }

    @Test
    public void testPolyLineXor()
    {
        final List<? extends PolyLine> clips = SUBJECT_POLYLINE.clip(CLIPPING, ClipType.XOR)
                .getClip();
        Assert.assertEquals(4, clips.size());
    }

    @Test
    public void testPolygonAnd()
    {
        final List<? extends PolyLine> clips = SUBJECT.clip(CLIPPING, ClipType.AND).getClip();
        Assert.assertEquals(2, clips.size());
    }

    @Test
    public void testPolygonNot()
    {
        final List<? extends PolyLine> clips = SUBJECT.clip(CLIPPING, ClipType.NOT).getClip();
        Assert.assertEquals(2, clips.size());
    }

    @Test
    public void testPolygonOr()
    {
        final List<? extends PolyLine> clips = SUBJECT.clip(CLIPPING, ClipType.OR).getClip();
        Assert.assertEquals(1, clips.size());
    }

    @Test
    public void testPolygonXor()
    {
        final List<? extends PolyLine> clips = SUBJECT.clip(CLIPPING, ClipType.XOR).getClip();
        Assert.assertEquals(4, clips.size());
    }
}
