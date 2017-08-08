package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * @author matthieun
 */
public class SubRelationList
{
    private final List<Relation> subRelations;
    private final Relation fixRelation;

    public SubRelationList(final List<Relation> subRelations, final Relation fixRelation)
    {
        if (subRelations == null)
        {
            throw new CoreException("Cannot have a null list of sub relations.");
        }
        this.subRelations = subRelations;
        this.fixRelation = fixRelation;
    }

    public Relation getFixRelation()
    {
        return this.fixRelation;
    }

    public List<Relation> getSubRelations()
    {
        return this.subRelations;
    }

    public boolean hasFixRelation()
    {
        return this.fixRelation != null;
    }

    public int size()
    {
        return this.subRelations.size() + (hasFixRelation() ? 1 : 0);
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        final StringList relations = new StringList();
        this.subRelations.forEach(relation -> relations.add(relation.toString()));
        builder.append("[SubRelations: ");
        builder.append(relations.join(System.lineSeparator()));
        if (hasFixRelation())
        {
            builder.append(System.lineSeparator());
            builder.append("Fix Relation: ");
            builder.append(this.fixRelation.toString());
        }
        builder.append("]");
        return builder.toString();
    }
}
