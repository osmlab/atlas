package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;

/**
 * Class to handle relation collection in {@link MultiAtlasRelationAggregator}.
 *
 * @author mkalender
 */
public final class TemporaryRelation
{
    private final Relation relation;
    private final RelationBean bean;

    public TemporaryRelation(final Relation relation)
    {
        this.relation = relation;
        this.bean = new RelationBean();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof TemporaryRelation)
        {
            final TemporaryRelation that = (TemporaryRelation) other;
            return this.getIdentifier() == that.getIdentifier();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.relation.hashCode();
    }

    @Override
    public String toString()
    {
        return "TemporaryRelation [relation=" + this.relation + ", bean=" + this.bean + "]";
    }

    protected void addMember(final TemporaryRelationMember member)
    {
        this.bean.addItem(member.getIdentifier(), member.getRole(), member.getType());
    }

    protected long getIdentifier()
    {
        return this.relation.getIdentifier();
    }

    protected RelationMemberList getOldMembers()
    {
        return this.relation.members();
    }

    protected long getOsmIdentifier()
    {
        return this.relation.getOsmIdentifier();
    }

    protected RelationBean getRelationBean()
    {
        return this.bean;
    }

    protected Map<String, String> getTags()
    {
        return this.relation.getTags();
    }
}
