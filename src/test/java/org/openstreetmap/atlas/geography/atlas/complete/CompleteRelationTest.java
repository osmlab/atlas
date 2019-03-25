package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class CompleteRelationTest
{
    @Rule
    public CompleteTestRule rule = new CompleteTestRule();

    @Test
    public void testBloatedEquals()
    {
        final RelationBean members1 = new RelationBean();
        members1.addItem(456L, "myRole", ItemType.AREA);
        final RelationBean members2 = new RelationBean();
        members2.addItem(789L, "myRole", ItemType.AREA);

        final List<Long> allRelationsWithSameOsmIdentifier1 = new ArrayList<>();
        allRelationsWithSameOsmIdentifier1.add(456L);
        final List<Long> allRelationsWithSameOsmIdentifier2 = new ArrayList<>();
        allRelationsWithSameOsmIdentifier2.add(789L);

        final CompleteRelation relation11 = new CompleteRelation(123L, null, null, null, null, null,
                null, null);
        final CompleteRelation relation12 = new CompleteRelation(123L, null, null, null, null, null,
                null, null);
        final CompleteRelation relation21 = new CompleteRelation(123L, null,
                Polygon.SILICON_VALLEY.bounds(), null, null, null, null, null);
        final CompleteRelation relation22 = new CompleteRelation(123L, null,
                Polygon.SILICON_VALLEY.bounds(), null, null, null, null, null);
        final CompleteRelation relation23 = new CompleteRelation(123L, null,
                Polygon.SILICON_VALLEY_2.bounds(), null, null, null, null, null);
        final CompleteRelation relation31 = new CompleteRelation(123L, Maps.hashMap("key", "value"),
                null, null, null, null, null, null);
        final CompleteRelation relation32 = new CompleteRelation(123L, Maps.hashMap("key", "value"),
                null, null, null, null, null, null);
        final CompleteRelation relation33 = new CompleteRelation(123L, Maps.hashMap(), null, null,
                null, null, null, null);
        final CompleteRelation relation41 = new CompleteRelation(123L, null, null, null, null, null,
                null, Sets.hashSet(1L, 2L));
        final CompleteRelation relation42 = new CompleteRelation(123L, null, null, null, null, null,
                null, Sets.hashSet(1L, 2L));
        final CompleteRelation relation43 = new CompleteRelation(123L, null, null, null, null, null,
                null, Sets.hashSet(1L));
        final CompleteRelation relation51 = new CompleteRelation(123L, null, null, members1, null,
                null, null, null);
        final CompleteRelation relation52 = new CompleteRelation(123L, null, null, members1, null,
                null, null, null);
        final CompleteRelation relation53 = new CompleteRelation(123L, null, null, members2, null,
                null, null, null);
        final CompleteRelation relation61 = new CompleteRelation(123L, null, null, null,
                allRelationsWithSameOsmIdentifier1, null, null, null);
        final CompleteRelation relation62 = new CompleteRelation(123L, null, null, null,
                allRelationsWithSameOsmIdentifier1, null, null, null);
        final CompleteRelation relation63 = new CompleteRelation(123L, null, null, null,
                allRelationsWithSameOsmIdentifier2, null, null, null);
        final CompleteRelation relation71 = new CompleteRelation(123L, null, null, null, null,
                members1, null, null);
        final CompleteRelation relation72 = new CompleteRelation(123L, null, null, null, null,
                members1, null, null);
        final CompleteRelation relation73 = new CompleteRelation(123L, null, null, null, null,
                members2, null, null);
        final CompleteRelation relation81 = new CompleteRelation(123L, null, null, null, null, null,
                456L, null);
        final CompleteRelation relation82 = new CompleteRelation(123L, null, null, null, null, null,
                456L, null);
        final CompleteRelation relation83 = new CompleteRelation(123L, null, null, null, null, null,
                789L, null);

        Assert.assertEquals(relation11, relation12);
        Assert.assertEquals(relation21, relation22);
        Assert.assertEquals(relation31, relation32);
        Assert.assertEquals(relation41, relation42);
        Assert.assertEquals(relation51, relation52);
        Assert.assertEquals(relation61, relation62);
        Assert.assertEquals(relation71, relation72);
        Assert.assertEquals(relation81, relation82);

        // Here bounds are considered a derivation of the rest, and thus do not trigger an un-equal.
        Assert.assertEquals(relation11, relation21);
        Assert.assertNotEquals(relation11, relation31);
        Assert.assertNotEquals(relation11, relation41);
        // Here bounds are considered a derivation of the rest, and thus do not trigger an un-equal.
        Assert.assertEquals(relation21, relation23);
        Assert.assertNotEquals(relation31, relation33);
        Assert.assertNotEquals(relation41, relation43);
        Assert.assertNotEquals(relation51, relation53);
        Assert.assertNotEquals(relation61, relation63);
        Assert.assertNotEquals(relation71, relation73);
        Assert.assertNotEquals(relation81, relation83);
    }

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Relation source = atlas.relation(22);
        final CompleteRelation result = CompleteRelation.from(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.members().asBean(), result.members().asBean());
        Assert.assertEquals(source.allKnownOsmMembers().asBean(),
                result.allKnownOsmMembers().asBean());
        Assert.assertEquals(source.osmRelationIdentifier(), result.osmRelationIdentifier());
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));
    }

    @Test
    public void testIsCompletelyShallow()
    {
        final CompleteRelation superShallow = new CompleteRelation(123L, null, null, null, null,
                null, null, null);
        Assert.assertTrue(superShallow.isCompletelyShallow());
    }

    @Test
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Relation source = atlas.relation(22);
        final CompleteRelation result = CompleteRelation.shallowFrom(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withMembersAndSource(new RelationMemberList(source.members()), source);
        Assert.assertEquals(source.members().asBean(), result.members().asBean());
        result.withMembersAndSource(source.members().asBean(), source, source.bounds());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.members().asBean(), result.members().asBean());
        result.withAllKnownOsmMembers(source.allKnownOsmMembers().asBean());
        Assert.assertEquals(source.allKnownOsmMembers().asBean(),
                result.allKnownOsmMembers().asBean());
        result.withOsmRelationIdentifier(source.osmRelationIdentifier());
        Assert.assertEquals(source.osmRelationIdentifier(), result.osmRelationIdentifier());
        result.withRelationIdentifiers(source.relations().stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet()));
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));
    }
}
