package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
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
        return subRelation().members();
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
