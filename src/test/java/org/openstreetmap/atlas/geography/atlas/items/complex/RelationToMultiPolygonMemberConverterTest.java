package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation.Ring;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author matthieun
 */
public class RelationToMultiPolygonMemberConverterTest
{
    private static final RelationToMultiPolygonMemberConverter INNER = new RelationToMultiPolygonMemberConverter(
            Ring.INNER);
    private static final RelationToMultiPolygonMemberConverter OUTER = new RelationToMultiPolygonMemberConverter(
            Ring.OUTER);

    private static final Location ONE = Location.TEST_6;
    private static final Location TWO = Location.TEST_2;
    private static final Location THR = Location.TEST_1;
    private static final Location FOR = Location.TEST_5;
    private static final Location FVE = Location.TEST_4;

    private static final Polygon OUTER_LOOP = new Polygon(ONE, TWO, THR, FOR, FVE);

    private static final Location SIX = Location.TEST_4;
    private static final Location SVN = Location.TEST_7;
    private static final Location EIT = Location.TEST_2;

    private static final Polygon INNER_LOOP = new Polygon(SIX, SVN, EIT);

    private Atlas atlas;

    @Before
    public void init()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addLine(0, new PolyLine(ONE, TWO), Maps.hashMap());
        builder.addLine(1, new PolyLine(TWO, THR), Maps.hashMap());
        builder.addLine(2, new PolyLine(THR, FOR), Maps.hashMap());
        builder.addLine(3, new PolyLine(FOR, FVE), Maps.hashMap());
        builder.addLine(4, new PolyLine(FVE, ONE), Maps.hashMap());

        builder.addLine(5, new PolyLine(SIX, SVN), Maps.hashMap());
        builder.addLine(6, new PolyLine(SVN, EIT), Maps.hashMap());
        builder.addLine(7, new PolyLine(EIT, SIX), Maps.hashMap());

        builder.addArea(0, INNER_LOOP, Maps.hashMap());

        final RelationBean bean = new RelationBean();

        bean.addItem(0L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.AREA);

        bean.addItem(0L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.LINE);
        bean.addItem(1L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.LINE);
        bean.addItem(2L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.LINE);
        bean.addItem(3L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.LINE);
        bean.addItem(4L, RelationTypeTag.MULTIPOLYGON_ROLE_OUTER, ItemType.LINE);

        bean.addItem(5L, RelationTypeTag.MULTIPOLYGON_ROLE_INNER, ItemType.LINE);
        bean.addItem(6L, RelationTypeTag.MULTIPOLYGON_ROLE_INNER, ItemType.LINE);
        bean.addItem(7L, RelationTypeTag.MULTIPOLYGON_ROLE_INNER, ItemType.LINE);

        builder.addRelation(0, 0, bean,
                Maps.hashMap(RelationTypeTag.KEY, RelationTypeTag.MULTIPOLYGON_TYPE));

        this.atlas = builder.get();
    }

    @Test
    public void testRings()
    {
        final List<Polygon> outers = Iterables.asList(OUTER.convert(this.atlas.relation(0)));
        Assert.assertTrue(outers.contains(OUTER_LOOP));
        Assert.assertTrue(outers.contains(INNER_LOOP));
        Assert.assertEquals(INNER_LOOP, INNER.convert(this.atlas.relation(0)).iterator().next());
    }
}
