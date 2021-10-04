package org.openstreetmap.atlas.geography.atlas.change;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class FeatureChangeTest
{
    @RegisterExtension
    static ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    /**
     * The arguments to use for {@link #testChangeDescriptionGeometryAtlasAddGeometry()}.
     *
     * @return The stream of arguments.
     */
    static Stream<Arguments> testChangeDescriptionGeometryAtlasDeleteGeometry()
    {
        return Stream.of(Arguments.of("line",
                // Node 3 is shared with area 3, so it is not deleted.
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><delete><way action=\"delete\" id=\"1\" if-unused=\"true\" version=\"1\" visible=\"true\"><tag k=\"name\" v=\"Something\"/><nd ref=\"1\"/><nd ref=\"2\"/></way><node action=\"delete\" id=\"1\" if-unused=\"true\" version=\"1\" visible=\"false\"/><node action=\"delete\" id=\"2\" if-unused=\"true\" version=\"1\" visible=\"false\"/></delete></osmChange>",
                (Supplier<AtlasEntity>) () -> rule.getGeometryChangeAtlas().line(1_000_000)),
                Arguments
                        .of("edge",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><delete><way action=\"delete\" id=\"2\" if-unused=\"true\" version=\"1\" visible=\"true\"><tag k=\"highway\" v=\"residential\"/><nd ref=\"4\"/><nd ref=\"5\"/></way><node action=\"delete\" id=\"4\" if-unused=\"true\" version=\"1\" visible=\"false\"/><node action=\"delete\" id=\"5\" if-unused=\"true\" version=\"1\" visible=\"false\"/></delete></osmChange>",
                                (Supplier<AtlasEntity>) () -> rule
                                        .getGeometryChangeAtlas().edge(2_000_000)),
                Arguments.of("area",
                        // Node 3 is shared with line 1, so it is not deleted.
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><delete><way action=\"delete\" id=\"3\" if-unused=\"true\" version=\"1\" visible=\"true\"><tag k=\"name\" v=\"Something\"/><nd ref=\"6\"/><nd ref=\"7\"/></way><node action=\"delete\" id=\"6\" if-unused=\"true\" version=\"1\" visible=\"false\"/><node action=\"delete\" id=\"7\" if-unused=\"true\" version=\"1\" visible=\"false\"/></delete></osmChange>",
                        (Supplier<AtlasEntity>) () -> rule.getGeometryChangeAtlas()
                                .area(3_000_000)));
    }

    @Test
    void testAfterViewIsFull()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null));
        assertFalse(featureChange1.afterViewIsFull());
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteArea(
                123L, Polygon.SILICON_VALLEY, Maps.hashMap("key1", "value2"), Sets.hashSet(123L)));
        assertTrue(featureChange2.afterViewIsFull());
    }

    @Test
    void testBeforeViewUsefulnessValidationArea()
    {
        final Polygon polygon = Polygon.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteArea before = new CompleteArea(123L, polygon, tags, relations);

        final CompleteArea after = CompleteArea.shallowFrom(before).withPolygon(polygon)
                .withTags(tags).withRelationIdentifiers(relations);

        final FeatureChangeUsefulnessValidator featureChangeUsefulnessValidator = new FeatureChangeUsefulnessValidator(
                new FeatureChange(ChangeType.ADD, after, before));
        final CoreException coreException = assertThrows(CoreException.class,
                featureChangeUsefulnessValidator::validate);
        assertTrue(coreException.getMessage().contains("is not useful"));
    }

    @Test
    void testBeforeViewUsefulnessValidationEdge()
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

        final FeatureChangeUsefulnessValidator featureChangeUsefulnessValidator = new FeatureChangeUsefulnessValidator(
                new FeatureChange(ChangeType.ADD, after, before));
        final CoreException coreException = assertThrows(CoreException.class,
                featureChangeUsefulnessValidator::validate);
        assertTrue(coreException.getMessage().contains("is not useful"));
    }

    @Test
    void testBeforeViewUsefulnessValidationLine()
    {
        final PolyLine line = PolyLine.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteLine before = new CompleteLine(123L, line, tags, relations);

        final CompleteLine after = CompleteLine.shallowFrom(before).withPolyLine(line)
                .withTags(tags).withRelationIdentifiers(relations);

        final FeatureChangeUsefulnessValidator featureChangeUsefulnessValidator = new FeatureChangeUsefulnessValidator(
                new FeatureChange(ChangeType.ADD, after, before));
        final CoreException coreException = assertThrows(CoreException.class,
                featureChangeUsefulnessValidator::validate);
        assertTrue(coreException.getMessage().contains("is not useful"));
    }

    @Test
    void testBeforeViewUsefulnessValidationNode()
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

        final FeatureChangeUsefulnessValidator featureChangeUsefulnessValidator = new FeatureChangeUsefulnessValidator(
                new FeatureChange(ChangeType.ADD, after, before));
        final CoreException coreException = assertThrows(CoreException.class,
                featureChangeUsefulnessValidator::validate);
        assertTrue(coreException.getMessage().contains("is not useful"));
    }

    @Test
    void testBeforeViewUsefulnessValidationPoint()
    {
        final Location location = Location.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompletePoint before = new CompletePoint(123L, location, tags, relations);

        final CompletePoint after = CompletePoint.shallowFrom(before).withLocation(location)
                .withTags(tags).withRelationIdentifiers(relations);

        final FeatureChangeUsefulnessValidator featureChangeUsefulnessValidator = new FeatureChangeUsefulnessValidator(
                new FeatureChange(ChangeType.ADD, after, before));
        final CoreException coreException = assertThrows(CoreException.class,
                featureChangeUsefulnessValidator::validate);
        assertTrue(coreException.getMessage().contains("is not useful"));
    }

    @Test
    void testBeforeViewUsefulnessValidationRelation()
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

        final FeatureChangeUsefulnessValidator featureChangeUsefulnessValidator = new FeatureChangeUsefulnessValidator(
                new FeatureChange(ChangeType.ADD, after, before));
        final CoreException coreException = assertThrows(CoreException.class,
                featureChangeUsefulnessValidator::validate);
        assertTrue(coreException.getMessage().contains("is not useful"));
    }

    @Test
    void testChangeDescriptionGeometry()
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

        assertEquals(goldenString, description.toString());
    }

    @Test
    void testChangeDescriptionGeometryAtlasAddGeometry()
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
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><create><node action=\"modify\" id=\"-1\" lat=\"0.0\" lon=\"0.0\" version=\"1\" visible=\"true\"/></create><modify><way action=\"modify\" id=\"1\" version=\"1\" visible=\"true\"><tag k=\"name\" v=\"Something\"/><nd ref=\"1\"/><nd ref=\"-1\"/><nd ref=\"2\"/><nd ref=\"3\"/></way></modify></osmChange>"
                                .getBytes(StandardCharsets.UTF_8))
                + "\"}";

        assertEquals(goldenString, description.toJsonElement().toString());
    }

    @Test
    void testChangeDescriptionGeometryAtlasChangeGeometry()
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
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><modify><way action=\"modify\" id=\"1\" version=\"1\" visible=\"true\"><tag k=\"name\" v=\"Something\"/><nd ref=\"3\"/><nd ref=\"2\"/><nd ref=\"1\"/></way></modify></osmChange>"
                                .getBytes(StandardCharsets.UTF_8))
                + "\"}";

        assertEquals(goldenString, description.toJsonElement().toString());
    }

    /**
     * Check various deletions for an entity
     *
     * @param name
     *            The name to show for the test
     * @param expectedOsc
     *            The expected OSC
     * @param atlasEntitySupplier
     *            The entity supplier (the entity will be "deleted")
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource
    void testChangeDescriptionGeometryAtlasDeleteGeometry(final String name,
            final String expectedOsc, final Supplier<AtlasEntity> atlasEntitySupplier)
    {
        final AtlasEntity atlasEntity = atlasEntitySupplier.get();
        final Atlas atlas = atlasEntity.getAtlas();

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.REMOVE,
                CompleteEntity.shallowFrom(atlasEntity));
        featureChange1.setOptions(FeatureChange.Options.OSC_IF_POSSIBLE);
        featureChange1.withAtlasContext(atlas);

        final ChangeDescription description = featureChange1.explain();

        final String goldenString = "{\"type\":\"REMOVE\"," + "\"descriptors\":[],\"osc\":\""
                + Base64.getEncoder().encodeToString(expectedOsc.getBytes(StandardCharsets.UTF_8))
                + "\"}";

        assertEquals(goldenString, description.toJsonElement().toString(),
                "Expected:" + System.lineSeparator() + expectedOsc + System.lineSeparator()
                        + "but got" + System.lineSeparator()
                        + new String(Base64.getDecoder().decode(description.toJsonElement()
                                .getAsJsonObject().getAsJsonPrimitive("osc").getAsString())));
    }

    @Test
    void testChangeDescriptionInOutEdges()
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

        assertEquals(goldenString, description.toString());
    }

    @Test
    void testChangeDescriptionParentRelations()
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

        assertEquals(goldenString, description.toString());
    }

    @Test
    void testChangeDescriptionRelationMember()
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

        assertEquals(goldenString, description.toString());
    }

    @Test
    void testChangeDescriptionStartEndNodes()
    {
        final PolyLine polyline1 = PolyLine
                .wkt("LINESTRING(1 1, 2 2, 3 3, 10 10, 20 20, 4 4, 5 5)");

        final CompleteEdge before3 = new CompleteEdge(123L, polyline1, null, 1L, 2L, null);
        final CompleteEdge after3 = new CompleteEdge(123L, polyline1, null, 10L, 20L, null);
        final FeatureChange featureChange3 = new FeatureChange(ChangeType.ADD, after3, before3);
        final ChangeDescription description = featureChange3.explain();

        final String goldenString = "ChangeDescription [\n" + "UPDATE EDGE 123\n"
                + "START_NODE(UPDATE, 1 => 10)\n" + "END_NODE(UPDATE, 2 => 20)\n" + "]";

        assertEquals(goldenString, description.toString());
    }

    @Test
    void testChangeDescriptionTag()
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

        assertEquals(goldenString, description.toString());
    }

    @Test
    void testExplicitBeforeView()
    {
        final Supplier<CompletePoint> completePointGenerator = () -> new CompletePoint(1000000L,
                Location.CENTER, Collections.emptyMap(), Collections.emptySet());
        final List<ChangeDescriptor> changeDescriptors = new FeatureChange(ChangeType.ADD,
                completePointGenerator.get().withAddedTag(NameTag.KEY, "test"),
                completePointGenerator.get()).explain().getChangeDescriptors();

        assertEquals(1, changeDescriptors.size());
        assertTrue(changeDescriptors.get(0) instanceof TagChangeDescriptor);
        assertEquals(ChangeDescriptorType.ADD, changeDescriptors.get(0).getChangeDescriptorType());
    }

    @Test
    void testShallowValidation()
    {
        final CompletePoint before = new CompletePoint(123L, Location.CENTER,
                Maps.hashMap("a", "1", "b", "2"), Sets.hashSet(1L, 2L));

        final CompletePoint after = CompletePoint.shallowFrom(before);
        final CoreException coreException = assertThrows(CoreException.class,
                () -> FeatureChange.add(after));
        assertTrue(coreException.getMessage().contains("was shallow"));
    }

    @Test
    void testTags()
    {
        final String key = "key1";
        final String value = "value1";
        final Map<String, String> tags = Maps.hashMap(key, value, "key2", "value2");
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.CENTER, tags, null));
        assertEquals(new HashMap<>(tags), featureChange.getTags());
        assertEquals(value, featureChange.getTag(key).get());
        assertTrue(featureChange.toString().contains(tags.toString()));
    }
}
