package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.MultiPolygon;
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
    public Optional<MultiPolygon> asMultiPolygon()
    {
        // return the previously stored result
        if (this.getBadGeom() || this.getGeom() != null)
        {
            return Optional.ofNullable(this.getGeom());
        }
        MultiPolygon relationGeometry = null;
        if (packedAtlas().containsEnhancedRelationGeometry())
        {
            relationGeometry = packedAtlas().relationGeometry(this.index);
            this.setGeom(relationGeometry);
        }
        if (relationGeometry == null)
        {
            return super.asMultiPolygon();
        }
        return Optional.ofNullable(relationGeometry);
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
    public Long osmRelationIdentifier()
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
