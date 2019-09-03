package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

/**
 * @author lcram
 */
public final class GeometryChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final AbstractDelta<Location> delta;
    private final int sourceMaterialSize;

    public static List<GeometryChangeDescriptor> getDescriptorsForGeometry(
            final List<Location> beforeList, final List<Location> afterList)
    {
        final Patch<Location> diff;
        try
        {
            diff = DiffUtils.diff(beforeList, afterList);
        }
        catch (final DiffException exception)
        {
            throw new CoreException("Failed to compute diff for GeometryChangeDescriptor",
                    exception);
        }

        final List<GeometryChangeDescriptor> descriptors = new ArrayList<>();
        for (final AbstractDelta<Location> delta : diff.getDeltas())
        {
            descriptors.add(new GeometryChangeDescriptor(delta, beforeList.size()));
        }

        return descriptors;
    }

    public static Patch getPatch(final List<GeometryChangeDescriptor> descriptors)
    {
        final Patch<Location> patch = new Patch<>();
        for (final GeometryChangeDescriptor descriptor : descriptors)
        {
            patch.addDelta(descriptor.getDelta());
        }
        return patch;
    }

    private GeometryChangeDescriptor(final AbstractDelta<Location> delta, final int sourceMaterialSize)
    {
        switch (delta.getType())
        {
            case CHANGE:
                this.changeType = ChangeDescriptorType.UPDATE;
                break;
            case DELETE:
                this.changeType = ChangeDescriptorType.REMOVE;
                break;
            case INSERT:
                this.changeType = ChangeDescriptorType.ADD;
                break;
            default:
                throw new CoreException("Unexpected Delta value: " + delta.getType());
        }
        this.delta = delta;
        this.sourceMaterialSize = sourceMaterialSize;
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    public AbstractDelta<Location> getDelta()
    {
        return this.delta;
    }

    public int getSourcePosition()
    {
        return this.delta.getSource().getPosition();
    }

    @Override
    public String toString()
    {
        final StringBuilder diffString = new StringBuilder();
        diffString.append(this.changeType.toString());
        diffString.append(", ");
        diffString.append(this.delta.getSource().getPosition());
        diffString.append("/");
        diffString.append(this.sourceMaterialSize);
        switch (this.changeType)
        {
            case UPDATE:
                diffString.append(", ");
                diffString.append(this.delta.getSource().getLines());
                diffString.append(" => ");
                diffString.append(this.delta.getTarget().getLines());
                break;
            case REMOVE:
                diffString.append(", ");
                diffString.append(this.delta.getSource().getLines());
                break;
            case ADD:
                diffString.append(", ");
                diffString.append(this.delta.getTarget().getLines());
                break;
            default:
                throw new CoreException("Unexpected ChangeType value: " + this.delta.getType());
        }
        return "GEOM(" + diffString.toString() + ")";
    }
}
