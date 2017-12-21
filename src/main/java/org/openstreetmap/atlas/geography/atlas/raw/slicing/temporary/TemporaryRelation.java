package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * The {@link TemporaryRelation} object, keeps track of the bare minimum information required to
 * create an Atlas {@link Relation}, namely the bean and all the tagging.
 *
 * @author mgostintsev
 */
public class TemporaryRelation extends TemporaryEntity
{
    private static final long serialVersionUID = -5916385459136695799L;

    private final RelationBean bean;

    public TemporaryRelation(final long identifier, final Map<String, String> tags)
    {
        super(identifier, tags);
        this.bean = new RelationBean();
    }

    public void addMember(final TemporaryRelationMember member)
    {
        this.bean.addItem(member.getIdentifier(), member.getRole(), member.getType());
    }

    public RelationBean getRelationBean()
    {
        return this.bean;
    }

    @Override
    public String toString()
    {
        return "[Temporary Relation=" + this.getIdentifier() + ", bean=" + this.bean + ", "
                + tagString() + "]";
    }

}
