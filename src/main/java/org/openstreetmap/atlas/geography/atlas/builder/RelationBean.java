package org.openstreetmap.atlas.geography.atlas.builder;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedRelation;

/**
 * @author matthieun
 */
public class RelationBean extends AbstractCollection<RelationBeanItem> implements Serializable
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

        public RelationBeanItem(final RelationBeanItem item)
        {
            this.identifier = item.identifier;
            this.role = item.role;
            this.type = item.type;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof RelationBeanItem)
            {
                final RelationBeanItem that = (RelationBeanItem) other;
                return this.getIdentifier().equals(that.getIdentifier())
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
            return "[" + this.type + ", " + this.identifier + ", " + this.role + "]";
        }
    }

    private static final long serialVersionUID = 8511830231633569713L;

    private final List<RelationBeanItem> beanItems;
    /**
     * This set has no concept of how many {@link RelationBeanItem}s of a given value have been
     * removed. Technically, OSM allows for duplicate {@link RelationBeanItem}s in a given relation.
     * However, these duplicates are disallowed by {@link PackedAtlas#relationMembers} and by
     * extension {@link PackedRelation#members}. As a result, we need not worry about that edge case
     * here.
     */
    private final Set<RelationBeanItem> explicitlyExcluded;

    public static RelationBean fromSet(final Set<RelationBeanItem> set)
    {
        final RelationBean bean = new RelationBean();
        for (final RelationBeanItem item : set)
        {
            bean.addItem(item);
        }
        return bean;
    }

    public static RelationBean mergeBeans(final RelationBean left, final RelationBean right)
    {
        final RelationBean result = new RelationBean();
        for (final RelationBeanItem leftItem : left)
        {
            if (!right.isExplicitlyExcluded(leftItem))
            {
                result.addItem(leftItem);
            }
        }
        for (final RelationBeanItem rightItem : right)
        {
            final Optional<RelationBeanItem> existingLeftItem = left
                    .getItemFor(rightItem.getIdentifier(), rightItem.getType());
            if (existingLeftItem.isPresent()
                    && existingLeftItem.get().getRole().equals(rightItem.getRole()))
            {
                // Role already exists, continue.
                continue;
            }
            if (!left.isExplicitlyExcluded(rightItem))
            {
                result.addItem(rightItem);
            }
        }
        left.explicitlyExcluded.forEach(result::addItemExplicitlyExcluded);
        right.explicitlyExcluded.forEach(result::addItemExplicitlyExcluded);
        return result;
    }

    public RelationBean()
    {
        this.beanItems = new ArrayList<>();
        this.explicitlyExcluded = new HashSet<>();
    }

    @Override
    public boolean add(final RelationBeanItem item)
    {
        this.addItem(item);
        return true;
    }

    public void addItem(final Long identifier, final String role, final ItemType itemType)
    {
        this.beanItems.add(new RelationBeanItem(identifier, role, itemType));
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
     * Get this {@link RelationBean} as a {@link List} of its {@link RelationBeanItem}s.
     *
     * @return the item list representing this bean
     */
    public List<RelationBeanItem> asList()
    {
        return new ArrayList<>(this.beanItems);
    }

    /**
     * Get this {@link RelationBean} as a {@link Map} from its constituent {@link RelationBeanItem}s
     * to their counts (Here, counts refers to the number of times a given {@link RelationBeanItem}
     * appears in the bean. While abnormal, duplicate bean items are technically allowed by OSM).
     * This method is useful for comparing the equality of two {@link RelationBean}s, since the map
     * representation intrinsically ignores the internal ordering of the constituent
     * {@link RelationBeanItem}s (this ordering is irrelevant as far as equality is concerned).
     *
     * @return the item map representing this bean
     */
    public Map<RelationBeanItem, Integer> asMap()
    {
        final Map<RelationBeanItem, Integer> result = new HashMap<>();

        for (final RelationBeanItem beanItem : this)
        {
            if (result.containsKey(beanItem))
            {
                int count = result.get(beanItem);
                count += 1;
                result.put(beanItem, count);
            }
            else
            {
                result.put(beanItem, 1);
            }
        }

        return result;
    }

    /**
     * Get this {@link RelationBean} as a {@link Set} from its constituent
     * {@link RelationBeanItem}s. This will ignore the fact that a {@link RelationBean} can
     * technically contain duplicate items.
     *
     * @return the set representing this bean
     */
    public Set<RelationBeanItem> asSet()
    {
        return new HashSet<>(this.asMap().keySet());
    }

    /**
     * Check if the two beans are the same, without looking at the bean order.
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
            return this.asMap().equals(that.asMap());
        }
        return false;
    }

    /**
     * Check if the two beans are the same, without looking at the bean order. Also, ensure that
     * their explicitlyExcluded sets match.
     *
     * @param other
     *            The other object
     * @return True if the other object satisfies {@link RelationBean#equals(Object)} AND has a
     *         matching explicitlyExcluded set.
     */
    public boolean equalsIncludingExplicitlyExcluded(final Object other)
    {
        if (other instanceof RelationBean)
        {
            return other instanceof RelationBean && this.equals(other)
                    && this.explicitlyExcluded.equals(((RelationBean) other).explicitlyExcluded);
        }
        return false;
    }

    public Set<RelationBeanItem> getExplicitlyExcluded()
    {
        return this.explicitlyExcluded;
    }

    public Optional<RelationBeanItem> getItemFor(final long identifier, final ItemType type)
    {
        for (int index = 0; index < this.beanItems.size(); index++)
        {
            if (this.beanItems.get(index).getIdentifier() == identifier
                    && this.beanItems.get(index).getType() == type)
            {
                return Optional.of(getItemFor(index));
            }
        }
        return Optional.empty();
    }

    public Optional<RelationBeanItem> getItemFor(final long identifier, final String role,
            final ItemType type)
    {
        for (int index = 0; index < this.beanItems.size(); index++)
        {
            if (this.beanItems.get(index).getIdentifier() == identifier
                    && role.equals(this.beanItems.get(index).getRole())
                    && this.beanItems.get(index).getType() == type)
            {
                return Optional.of(getItemFor(index));
            }
        }
        return Optional.empty();
    }

    public List<Long> getMemberIdentifiers()
    {
        return this.beanItems.stream().map(RelationBeanItem::getIdentifier)
                .collect(Collectors.toList());
    }

    public List<String> getMemberRoles()
    {
        return this.beanItems.stream().map(RelationBeanItem::getRole).collect(Collectors.toList());
    }

    public List<ItemType> getMemberTypes()
    {
        return this.beanItems.stream().map(RelationBeanItem::getType).collect(Collectors.toList());
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
    @Override
    public boolean isEmpty()
    {
        return this.beanItems.isEmpty();
    }

    @Override
    public Iterator<RelationBeanItem> iterator()
    {
        return this.beanItems.iterator();
    }

    public RelationBean merge(final RelationBean other)
    {
        return RelationBean.mergeBeans(this, other);
    }

    /**
     * @return The number of members in this {@link RelationBean}
     */
    @Override
    public int size()
    {
        return this.beanItems.size();
    }

    @Override
    public String toString()
    {
        return "RelationBean [" + this.beanItems + "]";
    }

    private RelationBeanItem getItemFor(final int index)
    {
        if (index < 0 || index >= size())
        {
            throw new CoreException("Invalid index {}", index);
        }
        return new RelationBeanItem(this.beanItems.get(index));
    }

    private boolean isExplicitlyExcluded(final RelationBeanItem relationBeanItem)
    {
        return this.explicitlyExcluded.contains(relationBeanItem);
    }
}
