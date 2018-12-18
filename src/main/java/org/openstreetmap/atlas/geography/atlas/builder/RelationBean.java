package org.openstreetmap.atlas.geography.atlas.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.common.collect.HashMultiset;

/**
 * @author matthieun
 */
public class RelationBean implements Serializable, Iterable<RelationBeanItem>
{
    /**
     * @author matthieun
     */
    public static class RelationBeanItem implements Serializable
    {
        private static final long serialVersionUID = 441160361936498695L;

        private final Long identifier;
        private final String role;
        private final ItemType type;

        public RelationBeanItem(final Long identifier, final String role, final ItemType type)
        {
            this.identifier = identifier;
            this.role = role;
            this.type = type;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof RelationBeanItem)
            {
                final RelationBeanItem that = (RelationBeanItem) other;
                return this.getIdentifier() == that.getIdentifier()
                        && this.getRole().equals(that.getRole())
                        && this.getType() == that.getType();
            }
            return false;
        }

        public Long getIdentifier()
        {
            return this.identifier;
        }

        public String getRole()
        {
            return this.role;
        }

        public ItemType getType()
        {
            return this.type;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.identifier, this.role, this.type);
        }

        @Override
        public String toString()
        {
            return this.type + ", " + this.identifier + ", " + this.role;
        }
    }

    private static final long serialVersionUID = 8511830231633569713L;

    private final List<Long> memberIdentifiers;
    private final List<String> memberRoles;
    private final List<ItemType> memberTypes;

    private final Set<RelationBeanItem> explicitlyExcluded;

    public RelationBean()
    {
        this.memberIdentifiers = new ArrayList<>();
        this.memberRoles = new ArrayList<>();
        this.memberTypes = new ArrayList<>();

        this.explicitlyExcluded = new HashSet<>();
    }

    public void addItem(final Long identifier, final String role, final ItemType itemType)
    {
        this.memberIdentifiers.add(identifier);
        this.memberRoles.add(role);
        this.memberTypes.add(itemType);
    }

    public void addItem(final RelationBeanItem item)
    {
        addItem(item.getIdentifier(), item.getRole(), item.getType());
    }

    public void addItemExplicitlyExcluded(final Long identifier, final String role,
            final ItemType itemType)
    {
        addItemExplicitlyExcluded(new RelationBeanItem(identifier, role, itemType));
    }

    public void addItemExplicitlyExcluded(final RelationBeanItem item)
    {
        this.explicitlyExcluded.add(item);
    }

    /**
     * Check if the two beans are the same, without looking at the List order.
     *
     * @param other
     *            The other object
     * @return True if the other object is a {@link RelationBean} and is equal regardless of order.
     */
    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof RelationBean)
        {
            final RelationBean that = (RelationBean) other;
            return Iterables.equals(HashMultiset.create(this.getMemberIdentifiers()),
                    HashMultiset.create(that.getMemberIdentifiers()))
                    && Iterables.equals(HashMultiset.create(this.getMemberRoles()),
                            HashMultiset.create(that.getMemberRoles()))
                    && Iterables.equals(HashMultiset.create(this.getMemberTypes()),
                            HashMultiset.create(that.getMemberTypes()));
        }
        return false;
    }

    public Set<RelationBeanItem> getExplicitlyExcluded()
    {
        return this.explicitlyExcluded;
    }

    public Optional<RelationBeanItem> getItemFor(final long identifier, final ItemType type)
    {
        for (int index = 0; index < this.memberIdentifiers.size(); index++)
        {
            if (this.memberIdentifiers.get(index) == identifier
                    && this.memberTypes.get(index) == type)
            {
                return Optional.of(getItemFor(index));
            }
        }
        return Optional.empty();
    }

    public Optional<RelationBeanItem> getItemFor(final long identifier, final String role,
            final ItemType type)
    {
        for (int index = 0; index < this.memberIdentifiers.size(); index++)
        {
            if (this.memberIdentifiers.get(index) == identifier
                    && role.equals(this.memberRoles.get(index))
                    && this.memberTypes.get(index) == type)
            {
                return Optional.of(getItemFor(index));
            }
        }
        return Optional.empty();
    }

    public List<Long> getMemberIdentifiers()
    {
        return this.memberIdentifiers;
    }

    public List<String> getMemberRoles()
    {
        return this.memberRoles;
    }

    public List<ItemType> getMemberTypes()
    {
        return this.memberTypes;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getMemberIdentifiers(), this.getMemberRoles(),
                this.getMemberTypes());
    }

    /**
     * @return True if this bean has no members
     */
    public boolean isEmpty()
    {
        return this.memberIdentifiers.isEmpty();
    }

    @Override
    public Iterator<RelationBeanItem> iterator()
    {
        final List<RelationBeanItem> result = new ArrayList<>();
        for (int index = 0; index < this.memberIdentifiers.size(); index++)
        {
            result.add(new RelationBeanItem(this.memberIdentifiers.get(index),
                    this.memberRoles.get(index), this.memberTypes.get(index)));
        }
        return result.iterator();
    }

    public RelationBean merge(final RelationBean other)
    {
        final RelationBean result = new RelationBean();
        for (final RelationBeanItem leftItem : this)
        {
            if (!other.isExplicitlyExcluded(leftItem))
            {
                result.addItem(leftItem);
            }
        }
        for (final RelationBeanItem rightItem : other)
        {
            final Optional<RelationBeanItem> existingLeftItem = this
                    .getItemFor(rightItem.getIdentifier(), rightItem.getType());
            if (existingLeftItem.isPresent()
                    && existingLeftItem.get().getRole().equals(rightItem.getRole()))
            {
                // Role already exists, continue.
                continue;
            }
            if (!this.isExplicitlyExcluded(rightItem))
            {
                result.addItem(rightItem);
            }
        }
        this.explicitlyExcluded.forEach(result::addItemExplicitlyExcluded);
        other.explicitlyExcluded.forEach(result::addItemExplicitlyExcluded);
        return result;
    }

    /**
     * @return The number of members in this {@link RelationBean}
     */
    public int size()
    {
        return this.memberIdentifiers.size();
    }

    @Override
    public String toString()
    {
        return "RelationBean [memberIdentifiers=" + this.memberIdentifiers + ", memberRoles="
                + this.memberRoles + ", memberTypes=" + this.memberTypes + "]";
    }

    private RelationBeanItem getItemFor(final int index)
    {
        if (index < 0 || index > this.memberIdentifiers.size())
        {
            throw new CoreException("Invalid index {}", index);
        }
        return new RelationBeanItem(this.memberIdentifiers.get(index), this.memberRoles.get(index),
                this.memberTypes.get(index));
    }

    private boolean isExplicitlyExcluded(final RelationBeanItem relationBeanItem)
    {
        return this.explicitlyExcluded.contains(relationBeanItem);
    }
}
