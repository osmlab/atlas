package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;

/**
 * @author lcram
 */
public class GeometryChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final Patch<Location> diff;

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
        try
        {
            this.diff = DiffUtils.diff(beforeList, afterList);
        }
        catch (final DiffException exception)
        {
            throw new CoreException("Failed to compute diff for GeometryChangeDescriptor",
                    exception);
        }
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    public Patch<Location> getDiff()
    {
        return this.diff;
    }

    @Override
    public String toString()
    {
        final StringBuilder diffString = new StringBuilder();
        for (int i = 0; i < this.diff.getDeltas().size(); i++)
        {
            final AbstractDelta<Location> delta = this.diff.getDeltas().get(i);
            diffString.append("{");
            switch (delta.getType())
            {
                case CHANGE:
                    diffString.append(ChangeDescriptorType.UPDATE.toString());
                    diffString.append(", ");
                    diffString.append(delta.getSource().getPosition());
                    diffString.append(", ");
                    diffString.append(delta.getSource().getLines());
                    diffString.append(" => ");
                    diffString.append(delta.getTarget().getLines());
                    break;
                case DELETE:
                    diffString.append(ChangeDescriptorType.REMOVE.toString());
                    diffString.append(", ");
                    diffString.append(delta.getSource().getPosition());
                    diffString.append(", ");
                    diffString.append(delta.getSource().getLines());
                    break;
                case INSERT:
                    diffString.append(ChangeDescriptorType.ADD.toString());
                    diffString.append(", ");
                    diffString.append(delta.getSource().getPosition());
                    diffString.append(", ");
                    diffString.append(delta.getTarget().getLines());
                    break;
                case EQUAL:
                    diffString.append(DeltaType.EQUAL.toString());
                    break;
                default:
                    throw new CoreException("Unexpected Delta value: " + delta.getType());
            }
            if (i == this.diff.getDeltas().size() - 1)
            {
                diffString.append("}");
            }
            else
            {
                diffString.append("}, ");
            }
        }
        return "GEOM(" + diffString.toString() + ")";
    }
}
