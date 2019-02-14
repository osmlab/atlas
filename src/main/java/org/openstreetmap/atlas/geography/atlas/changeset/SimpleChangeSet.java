package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

import com.google.common.collect.AbstractIterator;

/**
 * A simple implementation of {@link ChangeSet} interface. This should be good for most common use
 * cases but if necessary user can choose to implement their own or extend from this one.
 * <p>
 * <b>This implementation is not thread safe</b>
 * </p>
 *
 * @author Yiqing Jin
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public class SimpleChangeSet implements ChangeSet // NOSONAR
{
    private static final long serialVersionUID = -6499530503182134327L;

    // internal data structure containing all data objects. key should be combination of identifier,
    // ItemType and ChangeAction.
    private final Map<String, ChangeItem> map = new HashMap<>();
    private String version;
    private String description;

    private static String computeKey(final long identifier, final ItemType type,
            final ChangeAction action)
    {
        return identifier + type.toShortString() + action;
    }

    public SimpleChangeSet()
    {
        this.version = "unknown";
        this.description = "";
    }

    @Override
    public boolean add(final ChangeItem changeItem)
    {
        return this.map.put(computeKey(changeItem), changeItem) != null;
    }

    @Override
    public boolean addAll(final Collection<? extends ChangeItem> changeItems)
    {
        boolean state = false;
        for (final ChangeItem changeItem : changeItems)
        {
            state = add(changeItem) || state;
        }
        return state;
    }

    @Override
    public void clear()
    {
        this.map.clear();
    }

    @Override
    public boolean contains(final long identifier, final ItemType type)
    {
        for (final ChangeAction action : ChangeAction.values())
        {
            if (this.map.containsKey(computeKey(identifier, type, action)))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(final long identifier, final ItemType type, final ChangeAction action)
    {
        return this.map.containsKey(computeKey(identifier, type, action));
    }

    @Override
    public boolean contains(final Object object)
    {
        if (!(object instanceof ChangeItem))
        {
            return false;
        }
        return this.map.containsKey(computeKey((ChangeItem) object));
    }

    @Override
    public boolean containsAll(final Collection<?> collection)
    {
        return collection.stream().allMatch(this::contains);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (!(other instanceof SimpleChangeSet))
        {
            return false;
        }
        final SimpleChangeSet that = (SimpleChangeSet) other;
        return StringUtils.equals(this.getVersion(), that.getVersion())
                && StringUtils.equals(this.getDescription(), that.getDescription())
                && this.map.equals(that.map);
    }

    @Override
    public Optional<ChangeItem> get(final long identifier, final ItemType type)
    {
        for (final ChangeAction action : ChangeAction.values())
        {
            final ChangeItem item = this.map.get(computeKey(identifier, type, action));
            if (item != null)
            {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ChangeItem> get(final long identifier, final ItemType type,
            final ChangeAction action)
    {
        return Optional.ofNullable(this.map.get(computeKey(identifier, type, action)));
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public Iterable<String> getSourceNames()
    {
        final List<String> sourceNames = new ArrayList<>();
        this.iterator().forEachRemaining(changeItem -> sourceNames.add(changeItem.getSourceName()));
        return sourceNames;
    }

    @Override
    public String getVersion()
    {
        return this.version;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getVersion(), this.getDescription(), this.map);
    }

    @Override
    public boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    @Override
    public Iterator<ChangeItem> iterator()
    {
        return this.map.values().iterator();
    }

    @Override
    public Iterator<ChangeItem> iterator(final ChangeAction action)
    {
        return filterIterator(iterator(), item -> item.getAction() == action);
    }

    @Override
    public Iterator<ChangeItem> iterator(final ItemType type)
    {
        return filterIterator(iterator(), item -> item.getType() == type);
    }

    @Override
    public Iterator<ChangeItem> iterator(final ItemType type, final ChangeAction action)
    {
        return filterIterator(iterator(),
                item -> item.getType() == type && item.getAction() == action);
    }

    @Override
    public boolean remove(final Object obj)
    {
        return this.map.remove(computeKey((ChangeItem) obj)) != null;
    }

    @Override
    public boolean removeAll(final Collection<?> collection)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> collection)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDescription(final String description)
    {
        this.description = description;
    }

    @Override
    public void setVersion(final String version)
    {
        this.version = version;
    }

    @Override
    public int size()
    {
        return this.map.size();
    }

    @Override
    public Set<ChangeItem> subSet(final ChangeAction action)
    {
        return this.stream().filter(item -> item.getAction() == action).collect(Collectors.toSet());
    }

    @Override
    public Set<ChangeItem> subSet(final ItemType type)
    {
        return this.stream().filter(item -> item.getType() == type).collect(Collectors.toSet());
    }

    @Override
    public Set<ChangeItem> subSet(final ItemType type, final ChangeAction action)
    {
        return this.stream().filter(item -> item.getType() == type && item.getAction() == action)
                .collect(Collectors.toSet());
    }

    @Override
    public Object[] toArray()
    {
        return this.map.values().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] array)
    {
        return this.map.values().toArray(array);
    }

    private String computeKey(final ChangeItem item)
    {
        return computeKey(item.getIdentifier(), item.getType(), item.getAction());
    }

    private Iterator<ChangeItem> filterIterator(final Iterator<ChangeItem> parent,
            final Predicate<ChangeItem> predicate)
    {
        return new AbstractIterator<ChangeItem>()
        {
            @Override
            protected ChangeItem computeNext()
            {
                while (parent.hasNext())
                {
                    final ChangeItem item = parent.next();
                    if (item != null && predicate.test(item))
                    {
                        return item;
                    }
                }
                return endOfData();
            }
        };
    }
}
