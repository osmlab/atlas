package org.openstreetmap.atlas.geography.atlas.change;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.TagChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.validators.FeatureChangeUsefulnessValidator;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class FeatureChangeTest
{
    @Rule
    public ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAfterViewIsFull()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null));
        Assert.assertFalse(featureChange1.afterViewIsFull());
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteArea(
                123L, Polygon.SILICON_VALLEY, Maps.hashMap("key1", "value2"), Sets.hashSet(123L)));
        Assert.assertTrue(featureChange2.afterViewIsFull());
    }

    @Test
    public void testBeforeViewUsefulnessValidationArea()
    {
        final Polygon polygon = Polygon.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteArea before = new CompleteArea(123L, polygon, tags, relations);

        final CompleteArea after = CompleteArea.shallowFrom(before).withPolygon(polygon)
                .withTags(tags).withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationEdge()
    {
        final PolyLine line = PolyLine.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Long startNode = 1L;
        final Long endNode = 2L;
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteEdge before = new CompleteEdge(123L, line, tags, startNode, endNode,
                relations);

        final CompleteEdge after = CompleteEdge.shallowFrom(before).withPolyLine(line)
                .withTags(tags).withStartNodeIdentifier(startNode).withEndNodeIdentifier(endNode)
                .withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationLine()
    {
        final PolyLine line = PolyLine.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteLine before = new CompleteLine(123L, line, tags, relations);

        final CompleteLine after = CompleteLine.shallowFrom(before).withPolyLine(line)
                .withTags(tags).withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationNode()
    {
        final Location location = Location.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final SortedSet<Long> inEdges = Sets.treeSet(1L, 2L);
        final SortedSet<Long> outEdges = Sets.treeSet(3L, 4L);
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteNode before = new CompleteNode(123L, location, tags, inEdges, outEdges,
                relations);

        final CompleteNode after = CompleteNode.shallowFrom(before).withLocation(location)
                .withTags(tags).withInEdgeIdentifiers(inEdges).withOutEdgeIdentifiers(outEdges)
                .withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationPoint()
    {
        final Location location = Location.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompletePoint before = new CompletePoint(123L, location, tags, relations);

        final CompletePoint after = CompletePoint.shallowFrom(before).withLocation(location)
                .withTags(tags).withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationRelation()
    {
        final Rectangle bounds = Rectangle.TEST_RECTANGLE;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final RelationBean members = new RelationBean();
        members.addItem(new RelationBeanItem(1L, "role", ItemType.POINT));
        final List<Long> allRelationsWithSameOsmIdentifier = Arrays.asList(1L, 2L);
        final RelationBean allKnownOsmMembers = new RelationBean();
        allKnownOsmMembers.addItem(new RelationBeanItem(2L, "role2", ItemType.AREA));
        final Long osmRelationIdentifier = 456L;
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteRelation before = new CompleteRelation(123L, tags, bounds, members,
                allRelationsWithSameOsmIdentifier, allKnownOsmMembers, osmRelationIdentifier,
                relations);

        final CompleteRelation after = CompleteRelation.shallowFrom(before).withTags(tags)
                .withMembers(members, bounds).withRelationIdentifiers(relations)
                .withAllRelationsWithSameOsmIdentifier(allRelationsWithSameOsmIdentifier)
                .withAllKnownOsmMembers(allKnownOsmMembers)
                .withOsmRelationIdentifier(osmRelationIdentifier);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testChangeDescriptionGeometry()
    {
        final PolyLine polyline1 = PolyLine
                .wkt("LINESTRING(1 1, 2 2, 3 3, 10 10, 20 20, 4 4, 5 5)");
        final PolyLine polyline2 = PolyLine
                .wkt("LINESTRING(1 1, 3 3, -10 -10, -20 -20, 4 4, 5 5, 6 6)");

        final CompleteLine before1 = new CompleteLine(123L, polyline1, null, null);
        final CompleteLine after1 = new CompleteLine(123L, polyline2, null, null);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, after1, before1);
        final ChangeDescription description = featureChange1.explain();

        final String goldenString = "ChangeDescription [\n" + "UPDATE LINE 123\n"
                + "GEOMETRY(ADD, 7/7, POINT (6 6))\n"
                + "GEOMETRY(UPDATE, 3/7, LINESTRING (10 10, 20 20) => LINESTRING (-10 -10, -20 -20))\n"
                + "GEOMETRY(REMOVE, 1/7, POINT (2 2))\n" + "]";

        Assert.assertEquals(goldenString, description.toString());
    }

    @Test
    public void testChangeDescriptionGeometryAtlasAddGeometry()
    {
        final Atlas atlas = this.rule.getGeometryChangeAtlas();
        final Line line = atlas.line(1000000);
        final CompleteEntity<CompleteLine> reversedLine = (CompleteEntity<CompleteLine>) CompleteEntity
                .shallowFrom(line);
        final List<Location> locations = Iterables.asList(line.asPolyLine());
        locations.add(1, Location.CENTER);
        reversedLine.withGeometry(locations);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                (AtlasEntity) reversedLine);
        featureChange1.setOptions(FeatureChange.Options.OSC_IF_POSSIBLE);
        featureChange1.withAtlasContext(atlas);

        final ChangeDescription description = featureChange1.explain();

        final String goldenString = "{\"type\":\"UPDATE\"," + "\"descriptors\":["
                + "{\"name\":\"GEOMETRY\",\"type\":\"ADD\",\"position\":\"1/3\","
                + "\"afterView\":\"POINT (0 0)\"}],"
                // The OSC changes
                + "\"osc\":\""
                + Base64.getEncoder().encodeToString(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><create><node id=\"-1\" lat=\"0.0\" lon=\"0.0\" version=\"1\" visible=\"true\"/></create><modify><way id=\"1\" version=\"1\" visible=\"true\"><tag k=\"name\" v=\"Something\"/><nd ref=\"1\"/><nd ref=\"-1\"/><nd ref=\"2\"/><nd ref=\"3\"/></way></modify></osmChange>"
                                .getBytes(StandardCharsets.UTF_8))
                + "\"}";

        Assert.assertEquals(goldenString, description.toJsonElement().toString());
    }

    @Test
    public void testChangeDescriptionGeometryAtlasChangeGeometry()
    {
        final Atlas atlas = this.rule.getGeometryChangeAtlas();
        final Line line = atlas.line(1000000);
        final CompleteEntity<CompleteLine> reversedLine = (CompleteEntity<CompleteLine>) CompleteEntity
                .shallowFrom(line);
        final List<Location> locations = Iterables.asList(line.asPolyLine());
        Collections.reverse(locations);
        reversedLine.withGeometry(locations);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                (AtlasEntity) reversedLine);
        featureChange1.setOptions(FeatureChange.Options.OSC_IF_POSSIBLE);
        featureChange1.withAtlasContext(atlas);

        final ChangeDescription description = featureChange1.explain();

        final String goldenString = "{\"type\":\"UPDATE\"," + "\"descriptors\":["
                + "{\"name\":\"GEOMETRY\",\"type\":\"ADD\",\"position\":\"3/3\","
                + "\"afterView\":\"LINESTRING (-61.33285 15.429499, -61.336198 15.420563)\"},"
                + "{\"name\":\"GEOMETRY\",\"type\":\"REMOVE\",\"position\":\"0/3\","
                + "\"beforeView\":\"LINESTRING (-61.336198 15.420563, -61.33285 15.429499)\"}],"
                // The OSC changes
                + "\"osc\":\""
                + Base64.getEncoder().encodeToString(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><modify><way id=\"1\" version=\"1\" visible=\"true\"><tag k=\"name\" v=\"Something\"/><nd ref=\"3\"/><nd ref=\"2\"/><nd ref=\"1\"/></way></modify></osmChange>"
                                .getBytes(StandardCharsets.UTF_8))
                + "\"}";

        Assert.assertEquals(goldenString, description.toJsonElement().toString());
    }

    @Test
    public void testChangeDescriptionGeometryAtlasDeleteGeometry()
    {
        final Atlas atlas = this.rule.getGeometryChangeAtlas();
        final Line line = atlas.line(1000000);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.REMOVE,
                CompleteEntity.shallowFrom(line));
        featureChange1.setOptions(FeatureChange.Options.OSC_IF_POSSIBLE);
        featureChange1.withAtlasContext(atlas);

        final ChangeDescription description = featureChange1.explain();

        final String goldenString = "{\"type\":\"REMOVE\"," + "\"descriptors\":[],\"osc\":\""
                + Base64.getEncoder().encodeToString(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><delete><way id=\"1\" if-unused=\"true\" version=\"1\" visible=\"false\"><tag k=\"name\" v=\"Something\"/><nd ref=\"1\"/><nd ref=\"2\"/><nd ref=\"3\"/></way><node id=\"1\" if-unused=\"true\" version=\"1\" visible=\"false\"/><node id=\"2\" if-unused=\"true\" version=\"1\" visible=\"false\"/><node id=\"3\" if-unused=\"true\" version=\"1\" visible=\"false\"/></delete></osmChange>"
                                .getBytes(StandardCharsets.UTF_8))
                + "\"}";

        Assert.assertEquals(goldenString, description.toJsonElement().toString());
    }

    @Test
    public void testChangeDescriptionInOutEdges()
    {
        final CompleteNode before2 = new CompleteNode(123L, Location.forString("1,1"), null,
                Sets.treeSet(1L, 2L), Sets.treeSet(3L, 4L), null);
        final CompleteNode after2 = new CompleteNode(123L, Location.forString("1,1"), null,
                Sets.treeSet(2L, 3L), Sets.treeSet(4L, 5L), null);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, after2, before2);
        final ChangeDescription description = featureChange2.explain();

        final String goldenString = "ChangeDescription [\n" + "UPDATE NODE 123\n"
                + "IN_EDGE(ADD, 3)\n" + "IN_EDGE(REMOVE, 1)\n" + "OUT_EDGE(ADD, 5)\n"
                + "OUT_EDGE(REMOVE, 3)\n" + "]";

        Assert.assertEquals(goldenString, description.toString());
    }

    @Test
    public void testChangeDescriptionParentRelations()
    {
        final PolyLine polyline1 = PolyLine.wkt("LINESTRING(1 1, 2 2, 3 3, 4 4, 5 5)");

        final CompleteLine before1 = new CompleteLine(123L, polyline1, null,
                Sets.hashSet(1L, 2L, 3L));
        final CompleteLine after1 = new CompleteLine(123L, polyline1, null,
                Sets.hashSet(2L, 3L, 4L));
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, after1, before1);
        final ChangeDescription description = featureChange1.explain();

        final String goldenString = "ChangeDescription [\n" + "UPDATE LINE 123\n"
                + "PARENT_RELATION(ADD, 4)\n" + "PARENT_RELATION(REMOVE, 1)\n" + "]";

        Assert.assertEquals(goldenString, description.toString());
    }

    @Test
    public void testChangeDescriptionRelationMember()
    {
        final CompleteRelation before1 = new CompleteRelation(123L, null, Rectangle.TEST_RECTANGLE,
                null, null, null, null, null);
        final CompleteRelation after1 = new CompleteRelation(123L, null, Rectangle.TEST_RECTANGLE,
                null, null, null, null, null);

        final RelationBean bean1 = new RelationBean();
        bean1.addItem(123L, "myRole", ItemType.AREA);
        bean1.addItem(456L, "myRole", ItemType.AREA);
        final RelationBean bean2 = new RelationBean();
        bean2.addItem(456L, "myRole", ItemType.AREA);
        bean2.addItem(789L, "myRole", ItemType.AREA);

        before1.withMembers(bean1, Rectangle.TEST_RECTANGLE);
        after1.withMembers(bean2, Rectangle.TEST_RECTANGLE);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, after1, before1);
        final ChangeDescription description = featureChange1.explain();
        System.out.println(description);

        final String goldenString = "ChangeDescription [\n" + "UPDATE RELATION 123\n"
                + "RELATION_MEMBER(ADD, AREA, 789, myRole)\n"
                + "RELATION_MEMBER(REMOVE, AREA, 123, myRole)\n" + "]";

        Assert.assertEquals(goldenString, description.toString());
    }

    @Test
    public void testChangeDescriptionStartEndNodes()
    {
        final PolyLine polyline1 = PolyLine
                .wkt("LINESTRING(1 1, 2 2, 3 3, 10 10, 20 20, 4 4, 5 5)");

        final CompleteEdge before3 = new CompleteEdge(123L, polyline1, null, 1L, 2L, null);
        final CompleteEdge after3 = new CompleteEdge(123L, polyline1, null, 10L, 20L, null);
        final FeatureChange featureChange3 = new FeatureChange(ChangeType.ADD, after3, before3);
        final ChangeDescription description = featureChange3.explain();

        final String goldenString = "ChangeDescription [\n" + "UPDATE EDGE 123\n"
                + "START_NODE(UPDATE, 1 => 10)\n" + "END_NODE(UPDATE, 2 => 20)\n" + "]";

        Assert.assertEquals(goldenString, description.toString());
    }

    @Test
    public void testChangeDescriptionTag()
    {
        final PolyLine polyline1 = PolyLine
                .wkt("LINESTRING(1 1, 2 2, 3 3, 10 10, 20 20, 4 4, 5 5)");

        final CompleteEdge before3 = new CompleteEdge(123L, polyline1,
                Maps.hashMap("key0", "value0", "key1", "value1"), null, null, null);
        final CompleteEdge after3 = new CompleteEdge(123L, polyline1,
                Maps.hashMap("key1", "newValue1", "key2", "value2"), null, null, null);
        final FeatureChange featureChange3 = new FeatureChange(ChangeType.ADD, after3, before3);
        final ChangeDescription description = featureChange3.explain();

        final String goldenString = "ChangeDescription [\n" + "UPDATE EDGE 123\n"
                + "TAG(ADD, key2, value2)\n" + "TAG(UPDATE, key1, value1 => newValue1)\n"
                + "TAG(REMOVE, key0, value0)\n" + "]";

        Assert.assertEquals(goldenString, description.toString());
    }

    @Test
    public void testExplicitBeforeView()
    {
        final Supplier<CompletePoint> completePointGenerator = () -> new CompletePoint(1000000L,
                Location.CENTER, Collections.emptyMap(), Collections.emptySet());
        final List<ChangeDescriptor> changeDescriptors = new FeatureChange(ChangeType.ADD,
                completePointGenerator.get().withAddedTag(NameTag.KEY, "test"),
                completePointGenerator.get()).explain().getChangeDescriptors();

        Assert.assertEquals(1, changeDescriptors.size());
        Assert.assertTrue(changeDescriptors.get(0) instanceof TagChangeDescriptor);
        Assert.assertEquals(ChangeDescriptorType.ADD,
                changeDescriptors.get(0).getChangeDescriptorType());
    }

    @Test
    public void testShallowValidation()
    {
        final CompletePoint before = new CompletePoint(123L, Location.CENTER,
                Maps.hashMap("a", "1", "b", "2"), Sets.hashSet(1L, 2L));

        final CompletePoint after = CompletePoint.shallowFrom(before);
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("was shallow");
        FeatureChange.add(after);
    }

    @Test
    public void testTags()
    {
        final String key = "key1";
        final String value = "value1";
        final Map<String, String> tags = Maps.hashMap(key, value, "key2", "value2");
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.CENTER, tags, null));
        Assert.assertEquals(new HashMap<>(tags), featureChange.getTags());
        Assert.assertEquals(value, featureChange.getTag(key).get());
        Assert.assertTrue(featureChange.toString().contains(tags.toString()));
    }
}
