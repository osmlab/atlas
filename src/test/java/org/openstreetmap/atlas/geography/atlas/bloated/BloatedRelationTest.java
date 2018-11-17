package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;

/**
 * @author matthieun
 */
public class BloatedRelationTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Relation source = atlas.relation(22);
        final BloatedRelation result = BloatedRelation.fromRelation(source);
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
        final BloatedRelation result = BloatedRelation.shallowFromRelation(source);
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
