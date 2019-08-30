package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 */
public class GeometryChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final int diffIndex;
    private final int geometrySize;
    private Location beforeLocation;
    private Location afterLocation;

    public GeometryChangeDescriptor(final ChangeDescriptorType changeType, final Location before,
            final Location after)
    {
        this.changeType = changeType;
        this.diffIndex = 0;
        this.geometrySize = 1;
        this.beforeLocation = before;
        this.afterLocation = after;
    }

    public GeometryChangeDescriptor(final ChangeDescriptorType changeType, final PolyLine before,
            final PolyLine after)
    {
        this.changeType = changeType;
        this.diffIndex = 0;
        this.geometrySize = Math.max(before.size(), after.size());
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }
}
