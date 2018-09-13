package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;

/**
 * @author matthieun
 */
public class DynamicRelation extends Relation
{
    private static final long serialVersionUID = 7994622214805021474L;

    // Not index!
    private final long identifier;

    protected DynamicRelation(final DynamicAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public RelationMemberList allKnownOsmMembers()
    {
        /*
         * TODO this will return AtlasEntities which are not of type DynamicX. Ideally, we should be
         * recreating returned entities as DynamicX instead of the underlying PackedX or MultiX.
         */
        return subRelation().allKnownOsmMembers();
    }

    @Override
    public List<Relation> allRelationsWithSameOsmIdentifier()
    {
        return subRelation().allRelationsWithSameOsmIdentifier().stream()
                .map(relation -> new DynamicRelation(dynamicAtlas(), relation.getIdentifier()))
                .collect(Collectors.toList());
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return subRelation().getTags();
    }

    @Override
    public RelationMemberList members()
    {
        final RelationMemberList subRelationMemberList = subRelation().members();
        final List<RelationMember> newMemberList = new ArrayList<>();

        for (final RelationMember member : subRelationMemberList)
        {
            final AtlasEntity entity = member.getEntity();
            AtlasEntity dynamicEntity = null;
            switch (entity.getType())
            {
                case NODE:
                    dynamicEntity = new DynamicNode(dynamicAtlas(), entity.getIdentifier());
                    break;
                case EDGE:
                    dynamicEntity = new DynamicEdge(dynamicAtlas(), entity.getIdentifier());
                    break;
                case POINT:
                    dynamicEntity = new DynamicPoint(dynamicAtlas(), entity.getIdentifier());
                    break;
                case LINE:
                    dynamicEntity = new DynamicLine(dynamicAtlas(), entity.getIdentifier());
                    break;
                case AREA:
                    dynamicEntity = new DynamicArea(dynamicAtlas(), entity.getIdentifier());
                    break;
                case RELATION:
                    dynamicEntity = new DynamicRelation(dynamicAtlas(), entity.getIdentifier());
                    break;
                default:
                    throw new CoreException("Invalid entity type {}", entity.getType());
            }
            newMemberList.add(new RelationMember(member.getRole(), dynamicEntity,
                    member.getRelationIdentifier()));
        }

        return new RelationMemberList(newMemberList);
    }

    @Override
    public long osmRelationIdentifier()
    {
        return subRelation().osmRelationIdentifier();
    }

    @Override
    public Set<Relation> relations()
    {
        return subRelation().relations().stream()
                .map(relation -> new DynamicRelation(dynamicAtlas(), relation.getIdentifier()))
                .collect(Collectors.toSet());
    }

    private DynamicAtlas dynamicAtlas()
    {
        return (DynamicAtlas) this.getAtlas();
    }

    private Relation subRelation()
    {
        final Relation result = dynamicAtlas().subRelation(this.identifier);
        if (result != null)
        {
            return result;
        }
        else
        {
            throw new CoreException("DynamicAtlas {} moved too fast! {} {} is missing now.",
                    dynamicAtlas().getName(), this.getClass().getSimpleName(), this.identifier);
        }
    }
}
