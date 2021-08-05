package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.locationtech.jts.geom.MultiPolygon;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;

import com.google.common.collect.Sets;

/**
 * {@link Relation} made from a {@link MultiAtlas}.
 *
 * @author matthieun
 */
public class MultiRelation extends Relation
{
    private static final long serialVersionUID = 2377231271257992525L;

    // Not index!
    private final long identifier;

    private SubRelationList subRelations;

    protected MultiRelation(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public RelationMemberList allKnownOsmMembers()
    {
        final List<RelationMember> members = new ArrayList<>();
        for (final Relation candidate : multiAtlas()
                .relationAllRelationsWithSameOsmIdentifier(this.identifier))
        {
            candidate.members().forEach(relationMember -> members.add(relationMember));
        }
        return new RelationMemberList(members);
    }

    @Override
    public List<Relation> allRelationsWithSameOsmIdentifier()
    {
        return multiAtlas().relationAllRelationsWithSameOsmIdentifier(this.identifier);
    }

    @Override
    public Optional<MultiPolygon> asMultiPolygon()
    {
        return getSingleSubRelation().asMultiPolygon();
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        // They all should have the same tags
        return getSingleSubRelation().getTags();
    }

    @Override
    public RelationMemberList members()
    {
        // Use a TreeSet to make sure all the members are always in a deterministic order.
        // RelationMember(s) are always ordered by member identifier.
        final Set<RelationMember> members = new TreeSet<>();
        for (final Relation subRelation : getSubRelations().getSubRelations())
        {
            final RelationMemberList subMembers = subRelation.members();
            for (final RelationMember subMember : subMembers)
            {
                final AtlasEntity nonMulti = subMember.getEntity();
                final long nonMultiIdentifier = nonMulti.getIdentifier();
                AtlasEntity multiEntity = null;
                if (nonMulti instanceof Node)
                {
                    multiEntity = multiAtlas().node(nonMultiIdentifier);
                }
                else if (nonMulti instanceof Edge)
                {
                    multiEntity = multiAtlas().edge(nonMultiIdentifier);
                }
                else if (nonMulti instanceof Area)
                {
                    multiEntity = multiAtlas().area(nonMultiIdentifier);
                }
                else if (nonMulti instanceof Line)
                {
                    multiEntity = multiAtlas().line(nonMultiIdentifier);
                }
                else if (nonMulti instanceof Point)
                {
                    multiEntity = multiAtlas().point(nonMultiIdentifier);
                }
                else if (nonMulti instanceof Relation)
                {
                    multiEntity = multiAtlas().relation(nonMultiIdentifier);
                }
                else
                {
                    throw new CoreException("Could not find the proper type for {}", nonMulti);
                }
                if (multiEntity != null)
                {
                    members.add(new RelationMember(subMember.getRole(), multiEntity,
                            subMember.getRelationIdentifier()));
                }
            }
        }
        if (members.isEmpty())
        {
            throw new CoreException(
                    "This should not happen: MultiRelation {} has no members. Its sub relations are {}.",
                    getIdentifier(), getSubRelations());
        }
        return new RelationMemberList(members);
    }

    @Override
    public Long osmRelationIdentifier()
    {
        return getSingleSubRelation().osmRelationIdentifier();
    }

    @Override
    public Set<Relation> relations()
    {
        Set<Relation> unionOfAllParentRelations = new HashSet<>();
        for (final Relation subRelations : getSubRelations().getSubRelations())
        {
            final Set<Relation> currentSubRelationParentRelations = multiAtlas()
                    .multifyRelations(subRelations);
            unionOfAllParentRelations = Sets.union(unionOfAllParentRelations,
                    currentSubRelationParentRelations);
        }
        return unionOfAllParentRelations;
    }

    private Relation getSingleSubRelation()
    {
        return getSubRelations().getSubRelations().get(0);
    }

    private SubRelationList getSubRelations()
    {
        if (this.subRelations == null)
        {
            this.subRelations = this.multiAtlas().subRelations(this.identifier);
        }
        return this.subRelations;
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}
