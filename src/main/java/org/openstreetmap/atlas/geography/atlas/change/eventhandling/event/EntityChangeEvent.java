package org.openstreetmap.atlas.geography.atlas.change.eventhandling.event;

import java.io.Serializable;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;

/**
 * An abstract representation of a change in a
 * {@link org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity}.
 *
 * @author Yazad Khambata
 */
public abstract class EntityChangeEvent implements Serializable
{
    private static final long serialVersionUID = -9159065869173338344L;

    private final CompleteItemType completeItemType;
    private final long identifier;
    private final Optional<Object> newValue;

    public EntityChangeEvent(final CompleteItemType completeItemType, final long identifier)
    {
        this(completeItemType, identifier, Optional.empty());
    }

    public EntityChangeEvent(final CompleteItemType completeItemType, final long identifier,
            final Optional<Object> newValue)
    {
        super();
        this.completeItemType = completeItemType;
        this.identifier = identifier;
        this.newValue = newValue;
    }

    public CompleteItemType getCompleteItemType()
    {
        return this.completeItemType;
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    public Optional<Object> getNewValue()
    {
        return this.newValue;
    }
}
