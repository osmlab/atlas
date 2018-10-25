package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;

/**
 * Test for Relation producing its own bean
 *
 * @author jklamer
 */
public class RelationBeanTest
{
    @Rule
    public final RelationBeanTestRule setup = new RelationBeanTestRule();

    @Test
    public void testBeanCreation()
    {
        final Relation relation1 = this.setup.getAtlas().relation(1);
        final Relation relation2 = this.setup.getAtlas().relation(2);
        final Relation relation3 = this.setup.getAtlas().relation(3);

        final RelationBean relationBean1 = new RelationBean();
        relationBean1.addItem(1L, "outside", ItemType.NODE);

        final RelationBean relationBean2 = new RelationBean();
        relationBean2.addItem(1L, "inside", ItemType.NODE);
        relationBean2.addItem(2L, "outside", ItemType.NODE);
        relationBean2.addItem(6L, "outside", ItemType.NODE);

        final RelationBean relationBean3 = new RelationBean();
        relationBean3.addItem(3L, "outside", ItemType.NODE);
        relationBean3.addItem(4L, "front side", ItemType.NODE);
        relationBean3.addItem(5L, "outside", ItemType.NODE);

        Assert.assertEquals(relationBean1, relation1.getBean());
        Assert.assertEquals(relationBean2, relation2.getBean());
        Assert.assertEquals(relationBean3, relation3.getBean());
    }
}
