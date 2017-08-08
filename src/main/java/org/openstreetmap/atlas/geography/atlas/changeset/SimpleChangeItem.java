package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * A implementation of {@link ChangeItem} that should work for most cases.
 *
 * @author Yiqing Jin
 * @author mkalender
 */
public class SimpleChangeItem implements MutableChangeItem
{
    private static final long serialVersionUID = 3817694187693336803L;

    private String sourceName;
    private Map<String, String> tags;
    private long identifier;
    private ItemType type;
    private ChangeAction action;
    private Iterable<Location> geometry;
    private double score = 1;
    private final Set<ChangeItemMember> members;

    public SimpleChangeItem()
    {
        this.members = new HashSet<>();
    }

    public SimpleChangeItem(final long identifier, final String sourceName, final ItemType type,
            final ChangeAction action, final Iterable<Location> geometry,
            final Map<String, String> tags)
    {
        this.identifier = identifier;
        this.sourceName = sourceName;
        this.type = type;
        this.action = action;
        this.geometry = geometry;
        this.tags = tags;
        this.members = new HashSet<>();
    }

    @Override
    public void addAllMembers(final Iterable<ChangeItemMember> members) throws CoreException
    {
        assertTypeIsRelation();
        members.forEach(member -> this.members.add(member));
    }

    @Override
    public void addMember(final ChangeItemMember member) throws CoreException
    {
        assertTypeIsRelation();
        this.members.add(member);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (!(other instanceof SimpleChangeItem))
        {
            return false;
        }

        final SimpleChangeItem that = (SimpleChangeItem) other;
        return this.getIdentifier() == that.getIdentifier() && this.getType() == that.getType()
                && this.getAction() == that.getAction()
                && StringUtils.equals(this.getSourceName(), that.getSourceName())
                && this.getScore() == that.getScore()
                && Iterables.equals(this.getMembers(), that.getMembers())
                && Iterables.equals(this.getGeometry(), that.getGeometry());
    }

    @Override
    public ChangeAction getAction()
    {
        return this.action;
    }

    @Override
    public Iterable<Location> getGeometry()
    {
        return this.geometry;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Iterable<ChangeItemMember> getMembers()
    {
        return this.members;
    }

    @Override
    public Optional<RelationBean> getRelationBean()
    {
        assertTypeIsRelation();
        if (this.members.isEmpty())
        {
            return Optional.empty();
        }
        final RelationBean bean = new RelationBean();
        this.members.forEach(
                member -> bean.addItem(member.getIdentifier(), member.getRole(), member.getType()));
        return Optional.of(bean);
    }

    @Override
    public double getScore()
    {
        return this.score;
    }

    @Override
    public String getSourceName()
    {
        return this.sourceName;
    }

    @Override
    public Optional<String> getTag(final String key)
    {
        return Optional.ofNullable(this.tags.get(key));
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public ItemType getType()
    {
        return this.type;
    }

    @Override
    /**
     * ItemType and ChangeAction combined should have no more than 9 variants and ideally should
     * have less than 3. so identifier itself should be good enough for hash code
     */
    public int hashCode()
    {
        return (int) this.identifier;
    }

    @Override
    public boolean removeMember(final long identifier, final String role, final ItemType type)
            throws CoreException
    {
        assertTypeIsRelation();
        return this.members.remove(new SimpleChangeItemMember(identifier, role, type));
    }

    @Override
    public void setAction(final ChangeAction action)
    {
        this.action = action;
    }

    @Override
    public void setGeometry(final Iterable<Location> geometry) throws CoreException
    {
        assertTypeIsNotRelation();
        this.geometry = geometry;
    }

    @Override
    public void setIdentifier(final long identifier)
    {
        this.identifier = identifier;
    }

    public void setMembers(final Iterable<ChangeItemMember> members)
    {
        this.members.clear();
        this.addAllMembers(members);
    }

    @Override
    public void setScore(final double score)
    {
        this.score = score;
    }

    @Override
    public void setSourceName(final String sourceName)
    {
        this.sourceName = sourceName;
    }

    @Override
    public void setTags(final Map<String, String> tags)
    {
        this.tags = tags;
    }

    @Override
    public void setType(final ItemType type)
    {
        this.type = type;
    }

    private void assertTypeIsNotRelation()
    {
        if (this.getType() == ItemType.RELATION)
        {
            throw new CoreException("Cannot execute this on a ChangeItem with type {}",
                    this.getType());
        }
    }

    private void assertTypeIsRelation()
    {
        if (this.getType() != ItemType.RELATION)
        {
            throw new CoreException("Cannot execute this on a ChangeItem with type {}",
                    this.getType());
        }
    }
}
