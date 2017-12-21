package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.items.Line;

/**
 * The {@link TemporaryLine} object, keeps track of the bare minimum information required to create
 * an Atlas {@link Line}. It is meant to be as light-weight as possible, keeping track of only shape
 * point identifiers, instead of the actual Point objects themselves.
 *
 * @author mgostintsev
 */
public class TemporaryLine extends TemporaryEntity
{
    private static final long serialVersionUID = 3867946360797866502L;

    private final List<Long> shapePointIdentifiers;

    public TemporaryLine(final long identifier, final List<Long> shapePointIdentifiers,
            final Map<String, String> tags)
    {
        super(identifier, tags);
        this.shapePointIdentifiers = shapePointIdentifiers;
    }

    public List<Long> getShapePointIdentifiers()
    {
        return this.shapePointIdentifiers;
    }

    @Override
    public String toString()
    {
        return "[Temporary Line=" + this.getIdentifier() + ", shapePointIdentifiers="
                + this.getShapePointIdentifiers() + ", " + tagString() + "]";
    }
}
