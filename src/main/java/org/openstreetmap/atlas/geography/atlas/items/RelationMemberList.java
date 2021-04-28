package org.openstreetmap.atlas.geography.atlas.items;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedRelation;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 * @author Yazad Khambata
 */
public class RelationMemberList extends AbstractCollection<RelationMember> implements Located
{
    private final List<RelationMember> members;

    /**
     * This set has no concept of how many {@link RelationBeanItem}s of a given value have been
     * removed. Technically, OSM allows for duplicate {@link RelationBeanItem}s in a given relation.
     * However, these duplicates are disallowed by {@link PackedAtlas#relationMembers} and by
     * extension {@link PackedRelation#members}. As a result, we need not worry about that edge case
     * here.
     */
    private final Set<RelationBeanItem> explicitlyExcluded;

    /**
     * A {@link Collectors#collectingAndThen(Collector, Function)} wrapper for
     * {@link RelationMember}s.
     *
     * @return - the collector.
     */
    public static Collector<RelationMember, ? extends Object, RelationMemberList> collect()
    {
        return Collectors.collectingAndThen(Collectors.toList(), RelationMemberList::new);
    }

    public RelationMemberList(final Iterable<RelationMember> members)
    {
        if (members instanceof List)
        {
            this.members = (List<RelationMember>) members;
        }
        else
        {
            this.members = new ArrayList<>();
            members.forEach(this.members::add);
        }
        this.explicitlyExcluded = new HashSet<>();
    }

    @Override
    public boolean add(final RelationMember item)
    {
        return this.members.add(item);
    }

    public void addItemExplicitlyExcluded(final RelationBeanItem item)
    {
        this.explicitlyExcluded.add(item);
    }

    public RelationBean asBean()
    {
        final RelationBean result = new RelationBean();
        for (final RelationMember member : this.members)
        {
            result.addItem(member.getEntity().getIdentifier(), member.getRole(),
                    member.getEntity().getType());
        }
        this.explicitlyExcluded.forEach(result::addItemExplicitlyExcluded);
        return result;
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.forLocated(this.members);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof RelationMemberList)
        {
            final RelationMemberList that = (RelationMemberList) other;
            if (this.getMemberList().size() != that.getMemberList().size())
            {
                return false;
            }
            int index = 0;
            for (final RelationMember thisMember : this.members)
            {
                final RelationMember thatMember = that.get(index++);
                if (thisMember == null && thatMember != null
                        || thisMember != null && thatMember == null)
                {
                    return false;
                }
                if (thisMember == null && thatMember == null)
                {
                    continue;
                }
                if (!thisMember.equals(thatMember))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Check if the two {@link RelationMemberList}s are the same, without looking at the List order.
     * Also, ensure that their explicitlyExcluded sets match.
     *
     * @param other
     *            The other object
     * @return True if the other object satisfies {@link RelationMemberList#equals(Object)} AND has
     *         a matching explicitlyExcluded set.
     */
    public boolean equalsIncludingExplicitlyExcluded(final Object other)
    {
        if (other instanceof RelationMemberList)
        {
            final boolean basicEquals = this.equals(other);
            final RelationMemberList otherBean = (RelationMemberList) other;
            return basicEquals && this.explicitlyExcluded.equals(otherBean.explicitlyExcluded);
        }
        return false;
    }

    public RelationMember get(final int index)
    {
        if (index < 0 || index >= size())
        {
            throw new CoreException(
                    "No RelationMember with index {}. This list has only {} members.", index,
                    size());
        }
        return this.members.get(index);
    }

    public RelationMember get(final long identifier, final ItemType type)
    {
        for (final RelationMember member : this)
        {
            if (member.getEntity().getIdentifier() == identifier)
            {
                switch (type)
                {
                    case NODE:
                        if (member.getEntity() instanceof Node)
                        {
                            return member;
                        }
                        break;
                    case EDGE:
                        if (member.getEntity() instanceof Edge)
                        {
                            return member;
                        }
                        break;
                    case AREA:
                        if (member.getEntity() instanceof Area)
                        {
                            return member;
                        }
                        break;
                    case LINE:
                        if (member.getEntity() instanceof Line)
                        {
                            return member;
                        }
                        break;
                    case POINT:
                        if (member.getEntity() instanceof Point)
                        {
                            return member;
                        }
                        break;
                    case RELATION:
                        if (member.getEntity() instanceof Relation)
                        {
                            return member;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return null;
    }

    @Override
    public int hashCode()
    {
        return this.members.hashCode();
    }

    @Override
    public Iterator<RelationMember> iterator()
    {
        return Iterables.filter(this.members, member -> member != null).iterator();
    }

    @Override
    public int size()
    {
        return this.members.size();
    }

    @Override
    public String toString()
    {
        return this.members.toString();
    }

    private List<RelationMember> getMemberList()
    {
        return this.members;
    }
}
