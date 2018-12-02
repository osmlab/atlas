package org.openstreetmap.atlas.geography.atlas.bloated;

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
public class BloatedRelationTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

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

        final BloatedRelation relation11 = new BloatedRelation(123L, null, null, null, null, null,
                null, null);
        final BloatedRelation relation12 = new BloatedRelation(123L, null, null, null, null, null,
                null, null);
        final BloatedRelation relation21 = new BloatedRelation(123L, null,
                Polygon.SILICON_VALLEY.bounds(), null, null, null, null, null);
        final BloatedRelation relation22 = new BloatedRelation(123L, null,
                Polygon.SILICON_VALLEY.bounds(), null, null, null, null, null);
        final BloatedRelation relation23 = new BloatedRelation(123L, null,
                Polygon.SILICON_VALLEY_2.bounds(), null, null, null, null, null);
        final BloatedRelation relation31 = new BloatedRelation(123L, Maps.hashMap("key", "value"),
                null, null, null, null, null, null);
        final BloatedRelation relation32 = new BloatedRelation(123L, Maps.hashMap("key", "value"),
                null, null, null, null, null, null);
        final BloatedRelation relation33 = new BloatedRelation(123L, Maps.hashMap(), null, null,
                null, null, null, null);
        final BloatedRelation relation41 = new BloatedRelation(123L, null, null, null, null, null,
                null, Sets.hashSet(1L, 2L));
        final BloatedRelation relation42 = new BloatedRelation(123L, null, null, null, null, null,
                null, Sets.hashSet(1L, 2L));
        final BloatedRelation relation43 = new BloatedRelation(123L, null, null, null, null, null,
                null, Sets.hashSet(1L));
        final BloatedRelation relation51 = new BloatedRelation(123L, null, null, members1, null,
                null, null, null);
        final BloatedRelation relation52 = new BloatedRelation(123L, null, null, members1, null,
                null, null, null);
        final BloatedRelation relation53 = new BloatedRelation(123L, null, null, members2, null,
                null, null, null);
        final BloatedRelation relation61 = new BloatedRelation(123L, null, null, null,
                allRelationsWithSameOsmIdentifier1, null, null, null);
        final BloatedRelation relation62 = new BloatedRelation(123L, null, null, null,
                allRelationsWithSameOsmIdentifier1, null, null, null);
        final BloatedRelation relation63 = new BloatedRelation(123L, null, null, null,
                allRelationsWithSameOsmIdentifier2, null, null, null);
        final BloatedRelation relation71 = new BloatedRelation(123L, null, null, null, null,
                members1, null, null);
        final BloatedRelation relation72 = new BloatedRelation(123L, null, null, null, null,
                members1, null, null);
        final BloatedRelation relation73 = new BloatedRelation(123L, null, null, null, null,
                members2, null, null);
        final BloatedRelation relation81 = new BloatedRelation(123L, null, null, null, null, null,
                456L, null);
        final BloatedRelation relation82 = new BloatedRelation(123L, null, null, null, null, null,
                456L, null);
        final BloatedRelation relation83 = new BloatedRelation(123L, null, null, null, null, null,
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
        final BloatedRelation result = BloatedRelation.from(source);
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
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Relation source = atlas.relation(22);
        final BloatedRelation result = BloatedRelation.shallowFrom(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withMembers(new RelationMemberList(source.members()));
        Assert.assertEquals(source.members().asBean(), result.members().asBean());
        result.withMembers(source.members().asBean(), source.bounds());
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
