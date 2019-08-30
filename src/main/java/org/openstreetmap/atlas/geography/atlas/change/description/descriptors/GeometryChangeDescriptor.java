package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.utilities.collections.ListDiff;

/**
 * @author lcram
 */
public class GeometryChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final ListDiff.Diff<Location> diff;

    private static List<Location> makeList(final Location location)
    {
        final List<Location> list = new ArrayList<>();
        list.add(location);
        return list;
    }

    public GeometryChangeDescriptor(final ChangeDescriptorType changeType, final Location before,
            final Location after)
    {
        this(changeType, makeList(before), makeList(after));
    }

    public GeometryChangeDescriptor(final ChangeDescriptorType changeType, final PolyLine before,
            final PolyLine after)
    {
        this(changeType, new ArrayList<>(before), new ArrayList<>(after));
    }

    public GeometryChangeDescriptor(final ChangeDescriptorType changeType,
            final List<Location> beforeList, final List<Location> afterList)
    {
        this.changeType = changeType;
        this.diff = ListDiff.diff(beforeList, afterList);
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    public ListDiff.Diff<Location> getDiff()
    {
        return this.diff;
    }

    @Override
    public String toString()
    {
        return "GEOM(" + this.changeType + ", " + this.diff.toString() + ")";
    }
}
