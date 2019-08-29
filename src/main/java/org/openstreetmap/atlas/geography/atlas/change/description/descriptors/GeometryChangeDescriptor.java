package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 */
public class GeometryChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private int diffIndex;
    private Location beforeLocation;
    private Location afterLocation;

    public GeometryChangeDescriptor(final ChangeDescriptorType changeType, final Location before,
            final Location after)
    {
        this.changeType = changeType;
        this.diffIndex = 0;
        this.beforeLocation = before;
        this.afterLocation = after;
    }

    public GeometryChangeDescriptor(final ChangeDescriptorType changeType, final PolyLine before,
            final PolyLine after)
    {
        this.changeType = changeType;
        final List<Location> beforeLocations = new ArrayList<>(before);
        final List<Location> afterLocations = new ArrayList<>(after);
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }
}
