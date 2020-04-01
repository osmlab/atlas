package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * @author matthieun
 */
public class SubRelationList implements Iterable<Relation>, Serializable
{
    private static final long serialVersionUID = 8408824588171850810L;

    private final List<Relation> subRelations;

    SubRelationList(final List<Relation> subRelations)
    {
        if (subRelations == null)
        {
            throw new CoreException("Cannot have a null list of sub relations.");
        }
        this.subRelations = subRelations;
    }

    @Override
    public Iterator<Relation> iterator()
    {
        return this.subRelations.iterator();
    }

    public int size()
    {
        return this.subRelations.size();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        final StringList relations = new StringList();
        this.subRelations.forEach(relation -> relations.add(relation.toString()));
        builder.append("[SubRelations: ");
        builder.append(relations.join(System.lineSeparator()));
        builder.append("]");
        return builder.toString();
    }

    List<Relation> getSubRelations()
    {
        return this.subRelations;
    }
}
