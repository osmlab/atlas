package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author lcram
 */
public class ChangeDescriptorComparatorTest
{
    @Test
    public void testGlobalSorting()
    {
        final TagChangeDescriptor removeTag1 = new TagChangeDescriptor(ChangeDescriptorType.REMOVE,
                "keyRemove1", null, "valueRemove1");
        final TagChangeDescriptor removeTag2 = new TagChangeDescriptor(ChangeDescriptorType.REMOVE,
                "keyRemove2", null, "valueRemove2");
        final TagChangeDescriptor addTag1 = new TagChangeDescriptor(ChangeDescriptorType.ADD,
                "keyAdd1", "valueAdd1", null);
        final TagChangeDescriptor addTag2 = new TagChangeDescriptor(ChangeDescriptorType.ADD,
                "keyAdd2", "valueAdd2", null);
        final TagChangeDescriptor updateTag1 = new TagChangeDescriptor(ChangeDescriptorType.UPDATE,
                "keyUpdate1", "valueUpdate1Updated", "valueUpdate1");
        final TagChangeDescriptor updateTag2 = new TagChangeDescriptor(ChangeDescriptorType.UPDATE,
                "keyUpdate2", "valueUpdate2Updated", "valueUpdate2");
        final GeometryChangeDescriptor addGeometry1 = GeometryChangeDescriptor
                .getDescriptorsForGeometry(new ArrayList<>(),
                        Arrays.asList(Location.forString("1,1"), Location.forString("2,2")))
                .get(0);
        final GeometryChangeDescriptor addGeometry2 = GeometryChangeDescriptor
                .getDescriptorsForGeometry(new ArrayList<>(),
                        Arrays.asList(Location.forString("3,3"), Location.forString("4,4")))
                .get(0);
        final GeometryChangeDescriptor updateGeometry1 = GeometryChangeDescriptor
                .getDescriptorsForGeometry(
                        Arrays.asList(Location.forString("1,1"), Location.forString("2,2"),
                                Location.forString("3,3")),
                        Arrays.asList(Location.forString("1,1"), Location.forString("20,20"),
                                Location.forString("3,3")))
                .get(0);
        final GeometryChangeDescriptor updateGeometry2 = GeometryChangeDescriptor
                .getDescriptorsForGeometry(
                        Arrays.asList(Location.forString("1,1"), Location.forString("2,2"),
                                Location.forString("3,3")),
                        Arrays.asList(Location.forString("1,1"), Location.forString("2,2"),
                                Location.forString("30,30")))
                .get(0);
        final GeometryChangeDescriptor removeGeometry1 = GeometryChangeDescriptor
                .getDescriptorsForGeometry(
                        Arrays.asList(Location.forString("1,1"), Location.forString("2,2"),
                                Location.forString("3,3")),
                        Arrays.asList(Location.forString("1,1"), Location.forString("3,3")))
                .get(0);
        final GeometryChangeDescriptor removeGeometry2 = GeometryChangeDescriptor
                .getDescriptorsForGeometry(
                        Arrays.asList(Location.forString("1,1"), Location.forString("2,2"),
                                Location.forString("3,3")),
                        Arrays.asList(Location.forString("1,1"), Location.forString("2,2")))
                .get(0);
        final GenericElementChangeDescriptor<Long> addParentRelation1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 1L, ChangeDescriptorName.PARENT_RELATION);
        final GenericElementChangeDescriptor<Long> addParentRelation2 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 2L, ChangeDescriptorName.PARENT_RELATION);
        final GenericElementChangeDescriptor<Long> removeParentRelation1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.REMOVE, 1L, ChangeDescriptorName.PARENT_RELATION);
        final GenericElementChangeDescriptor<Long> removeParentRelation2 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.REMOVE, 2L, ChangeDescriptorName.PARENT_RELATION);
        final RelationMemberChangeDescriptor addRelationMember1 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.ADD, 1L, ItemType.POINT, "a");
        final RelationMemberChangeDescriptor addRelationMember2 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.ADD, 2L, ItemType.POINT, "a");
        final RelationMemberChangeDescriptor addRelationMember3 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.ADD, 2L, ItemType.POINT, "b");
        final RelationMemberChangeDescriptor addRelationMember4 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.ADD, 1L, ItemType.RELATION, "a");
        final RelationMemberChangeDescriptor removeRelationMember1 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.REMOVE, 1L, ItemType.POINT, "a");
        final RelationMemberChangeDescriptor removeRelationMember2 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.REMOVE, 2L, ItemType.POINT, "a");
        final RelationMemberChangeDescriptor removeRelationMember3 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.REMOVE, 2L, ItemType.POINT, "b");
        final RelationMemberChangeDescriptor removeRelationMember4 = new RelationMemberChangeDescriptor(
                ChangeDescriptorType.REMOVE, 1L, ItemType.RELATION, "a");
        final GenericElementChangeDescriptor<Long> addInEdge1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 1L, ChangeDescriptorName.IN_EDGE);
        final GenericElementChangeDescriptor<Long> addInEdge2 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 2L, ChangeDescriptorName.IN_EDGE);
        final GenericElementChangeDescriptor<Long> removeInEdge1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.REMOVE, 1L, ChangeDescriptorName.IN_EDGE);
        final GenericElementChangeDescriptor<Long> removeInEdge2 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.REMOVE, 2L, ChangeDescriptorName.IN_EDGE);
        final GenericElementChangeDescriptor<Long> addOutEdge1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 1L, ChangeDescriptorName.OUT_EDGE);
        final GenericElementChangeDescriptor<Long> addOutEdge2 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 2L, ChangeDescriptorName.OUT_EDGE);
        final GenericElementChangeDescriptor<Long> removeOutEdge1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.REMOVE, 1L, ChangeDescriptorName.OUT_EDGE);
        final GenericElementChangeDescriptor<Long> removeOutEdge2 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.REMOVE, 2L, ChangeDescriptorName.OUT_EDGE);
        final GenericElementChangeDescriptor<Long> addStartNode1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 1L, ChangeDescriptorName.START_NODE);
        final GenericElementChangeDescriptor<Long> updateStartNode1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.UPDATE, 1L, 10L, ChangeDescriptorName.START_NODE);
        final GenericElementChangeDescriptor<Long> addEndNode1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.ADD, 1L, ChangeDescriptorName.END_NODE);
        final GenericElementChangeDescriptor<Long> updateEndNode1 = new GenericElementChangeDescriptor<>(
                ChangeDescriptorType.UPDATE, 1L, 10L, ChangeDescriptorName.END_NODE);

        final List<ChangeDescriptor> goldenList = Arrays.asList(addTag1, addTag2, updateTag1,
                updateTag2, removeTag1, removeTag2, addGeometry1, addGeometry2, updateGeometry1,
                updateGeometry2, removeGeometry1, removeGeometry2, addParentRelation1,
                addParentRelation2, removeParentRelation1, removeParentRelation2,
                addRelationMember1, addRelationMember2, addRelationMember3, addRelationMember4,
                removeRelationMember1, removeRelationMember2, removeRelationMember3,
                removeRelationMember4, addInEdge1, addInEdge2, removeInEdge1, removeInEdge2,
                addOutEdge1, addOutEdge2, removeOutEdge1, removeOutEdge2, addStartNode1,
                updateStartNode1, addEndNode1, updateEndNode1);
        final List<ChangeDescriptor> actualList = new ArrayList<>(goldenList);
        Collections.shuffle(actualList);
        actualList.sort(new ChangeDescriptorComparator());

        Assert.assertEquals(goldenList, actualList);
    }
}
