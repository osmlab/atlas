package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import java.util.Arrays;
import java.util.List;

/**
 * @author Yazad Khambata
 */
public class TagChangeTest {

    @Rule
    public TagChangeTestRule tagChangeTestRule = new TagChangeTestRule();

    private final List<Class<? extends AtlasEntity>> atlasEntityClasses = Arrays.asList(Node.class, Point.class, Edge.class, Line.class, Area.class, Relation.class);

    private String addedTagKey = "added";

    private String addedTagValue1 = "me";
    private String addedTagValue2 = "you";
    private String addedTagValueBlank = "";
    private String addedTagValueNull = null;

    @Test
    public void testInsertNewTag() {
        final Atlas atlas = tagChangeTestRule.getAtlas();

        final CompleteNode completeNode = CompleteNode.shallowFrom(atlas.node(1)).withReplacedTag(addedTagKey, addedTagKey, addedTagValue1);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, completeNode);

        //ChangeBuilder changeBuilder = ChangeBuilder;
    }

    @Test
    public void testInsert2() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testInsertDuplicate1() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testUpdate1() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testUpdate2() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testUpdate3() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }


    @Test
    public void testUpsert1() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testUpsert2() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testUpsert3() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testDelete1() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testDelete2() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testDeleteNOP1() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }

    @Test
    public void testDeleteNOP2() {
        //System.out.println(tagChangeTestRule.getAtlas());
    }
}
