package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;

/**
 * @author samg
 */
public class MultiplePolyLineToMultiPolygonConverterTest
{
    private static final MultiplePolyLineToMultiPolygonConverter CONVERTER = new MultiplePolyLineToMultiPolygonConverter();

    private static final Location INNER_LOCATION_1 = Location.forString("24, 68");
    private static final Location INNER_LOCATION_2 = Location.forString("24, 69");
    private static final Location INNER_LOCATION_3 = Location.forString("23, 69");
    private static final Location INNER_LOCATION_4 = Location.forString("23, 68");

    private static final Location OUTER_LOCATION_1 = Location.forString("25, 67");
    private static final Location OUTER_LOCATION_2 = Location.forString("25, 70");
    private static final Location OUTER_LOCATION_3 = Location.forString("22, 70");
    private static final Location OUTER_LOCATION_4 = Location.forString("22, 67");

    @Test
    public void testConversion()
    {
        final PolyLine inner1 = new PolyLine(INNER_LOCATION_1, INNER_LOCATION_2);
        final PolyLine inner2 = new PolyLine(INNER_LOCATION_2, INNER_LOCATION_3);
        final PolyLine inner3 = new PolyLine(INNER_LOCATION_3, INNER_LOCATION_4);
        final PolyLine inner4 = new PolyLine(INNER_LOCATION_4, INNER_LOCATION_1);
        final List<PolyLine> inners = new ArrayList<>();
        inners.add(inner1);
        inners.add(inner2);
        inners.add(inner3);
        inners.add(inner4);

        final PolyLine outer1 = new PolyLine(OUTER_LOCATION_1, OUTER_LOCATION_2);
        final PolyLine outer2 = new PolyLine(OUTER_LOCATION_2, OUTER_LOCATION_3);
        final PolyLine outer3 = new PolyLine(OUTER_LOCATION_3, OUTER_LOCATION_4);
        final PolyLine outer4 = new PolyLine(OUTER_LOCATION_4, OUTER_LOCATION_1);
        final List<PolyLine> outers = new ArrayList<>();
        outers.add(outer1);
        outers.add(outer2);
        outers.add(outer3);
        outers.add(outer4);

        final Map<Ring, Iterable<PolyLine>> innersAndOuters = new HashMap<>();
        innersAndOuters.put(Ring.OUTER, outers);
        innersAndOuters.put(Ring.INNER, inners);

        final MultiPolygon result = CONVERTER.convert(innersAndOuters);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isSimplePolygon());
        Assert.assertEquals(1, result.inners().size());
        Assert.assertEquals(1, result.outers().size());
    }

    @Test(expected = MultiplePolyLineToPolygonsConverter.OpenPolygonException.class)
    public void testInnerFailstoClose()
    {
        final PolyLine inner1 = new PolyLine(INNER_LOCATION_1, INNER_LOCATION_2);
        final PolyLine inner2 = new PolyLine(INNER_LOCATION_2, INNER_LOCATION_3);
        final PolyLine inner3 = new PolyLine(INNER_LOCATION_3, INNER_LOCATION_4);
        final PolyLine inner4 = new PolyLine(INNER_LOCATION_4, INNER_LOCATION_1);
        final List<PolyLine> inners = new ArrayList<>();
        inners.add(inner1);
        inners.add(inner3);
        inners.add(inner4);

        final PolyLine outer1 = new PolyLine(OUTER_LOCATION_1, OUTER_LOCATION_2);
        final PolyLine outer2 = new PolyLine(OUTER_LOCATION_2, OUTER_LOCATION_3);
        final PolyLine outer3 = new PolyLine(OUTER_LOCATION_3, OUTER_LOCATION_4);
        final PolyLine outer4 = new PolyLine(OUTER_LOCATION_4, OUTER_LOCATION_1);
        final List<PolyLine> outers = new ArrayList<>();
        outers.add(outer1);
        outers.add(outer2);
        outers.add(outer3);
        outers.add(outer4);

        final Map<Ring, Iterable<PolyLine>> innersAndOuters = new HashMap<>();
        innersAndOuters.put(Ring.OUTER, outers);
        innersAndOuters.put(Ring.INNER, inners);

        final MultiPolygon result = CONVERTER.convert(innersAndOuters);
    }

    @Test(expected = CoreException.class)
    public void testNoOuter()
    {
        final PolyLine inner1 = new PolyLine(INNER_LOCATION_1, INNER_LOCATION_2);
        final PolyLine inner2 = new PolyLine(INNER_LOCATION_2, INNER_LOCATION_3);
        final PolyLine inner3 = new PolyLine(INNER_LOCATION_3, INNER_LOCATION_4);
        final PolyLine inner4 = new PolyLine(INNER_LOCATION_4, INNER_LOCATION_1);
        final List<PolyLine> inners = new ArrayList<>();
        inners.add(inner1);
        inners.add(inner2);
        inners.add(inner3);
        inners.add(inner4);

        final List<PolyLine> outers = new ArrayList<>();

        final Map<Ring, Iterable<PolyLine>> innersAndOuters = new HashMap<>();
        innersAndOuters.put(Ring.OUTER, outers);
        innersAndOuters.put(Ring.INNER, inners);

        final MultiPolygon result = CONVERTER.convert(innersAndOuters);
    }
}
