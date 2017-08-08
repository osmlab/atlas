package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;

/**
 * @author matthieun
 */
public class PackedRelation extends Relation
{
    private static final long serialVersionUID = -1912941368972318403L;

    private final long index;

    protected PackedRelation(final PackedAtlas atlas, final long index)
    {
        super(atlas);
        this.index = index;
    }

    @Override
    public RelationMemberList allKnownOsmMembers()
    {
        return packedAtlas().relationAllKnownOsmMembers(this.index);
    }

    @Override
    public List<Relation> allRelationsWithSameOsmIdentifier()
    {
        return packedAtlas().relationAllRelationsWithSameOsmIdentifier(this.index);
    }

    @Override
    public long getIdentifier()
    {
        return packedAtlas().relationIdentifier(this.index);
    }

    @Override
    public Map<String, String> getTags()
    {
        return packedAtlas().relationTags(this.index);
    }

    @Override
    public RelationMemberList members()
    {
        return packedAtlas().relationMembers(this.index);
    }

    @Override
    public long osmRelationIdentifier()
    {
        return packedAtlas().relationOsmIdentifier(this.index);
    }

    @Override
    public Set<Relation> relations()
    {
        return packedAtlas().relationRelations(this.index);
    }

    private PackedAtlas packedAtlas()
    {
        return (PackedAtlas) getAtlas();
    }
}
