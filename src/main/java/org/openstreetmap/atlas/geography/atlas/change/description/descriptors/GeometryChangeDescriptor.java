package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A {@link ChangeDescriptor} for geometry changes. Utilizes a granular diff algorithm to show the
 * individual {@link Location}s within the linestring that actually changed.
 * 
 * @author lcram
 */
public final class GeometryChangeDescriptor implements ChangeDescriptor
{
    private static final int TRUNCATE_WIDTH = 200;

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

    private GeometryChangeDescriptor(final AbstractDelta<Location> delta,
            final int sourceMaterialSize)
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

    public Optional<String> getAfterViewWkt()
    {
        if (this.changeType == ChangeDescriptorType.ADD
                || this.changeType == ChangeDescriptorType.UPDATE)
        {
            return Optional.of(new PolyLine(this.delta.getTarget().getLines()).toWkt());
        }
        return Optional.empty();
    }

    public Optional<String> getBeforeViewWkt()
    {
        if (this.changeType == ChangeDescriptorType.REMOVE
                || this.changeType == ChangeDescriptorType.UPDATE)
        {
            return Optional.of(new PolyLine(this.delta.getSource().getLines()).toWkt());
        }
        return Optional.empty();
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

    @Override
    public ChangeDescriptorName getName()
    {
        return ChangeDescriptorName.GEOMETRY;
    }

    public int getSourcePosition()
    {
        return this.delta.getSource().getPosition();
    }

    @Override
    public JsonElement toJsonElement()
    {
        final JsonObject descriptor = (JsonObject) ChangeDescriptor.super.toJsonElement();
        descriptor.addProperty("position",
                this.delta.getSource().getPosition() + "/" + this.sourceMaterialSize);
        switch (this.changeType)
        {
            case UPDATE:
                descriptor.addProperty("beforeView", StringUtils.truncate(
                        new PolyLine(this.delta.getSource().getLines()).toWkt(), TRUNCATE_WIDTH));
                descriptor.addProperty("afterView", StringUtils.truncate(
                        new PolyLine(this.delta.getTarget().getLines()).toWkt(), TRUNCATE_WIDTH));
                break;
            case REMOVE:
                descriptor.addProperty("beforeView", StringUtils.truncate(
                        new PolyLine(this.delta.getSource().getLines()).toWkt(), TRUNCATE_WIDTH));
                break;
            case ADD:
                descriptor.addProperty("afterView", StringUtils.truncate(
                        new PolyLine(this.delta.getTarget().getLines()).toWkt(), TRUNCATE_WIDTH));
                break;
            default:
                throw new CoreException("Unexpected ChangeType value: " + this.delta.getType());
        }
        return descriptor;
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
                diffString.append(new PolyLine(this.delta.getSource().getLines()).toWkt());
                diffString.append(" => ");
                diffString.append(new PolyLine(this.delta.getTarget().getLines()).toWkt());
                break;
            case REMOVE:
                diffString.append(", ");
                diffString.append(new PolyLine(this.delta.getSource().getLines()).toWkt());
                break;
            case ADD:
                diffString.append(", ");
                diffString.append(new PolyLine(this.delta.getTarget().getLines()).toWkt());
                break;
            default:
                throw new CoreException("Unexpected ChangeType value: " + this.delta.getType());
        }
        return getName().toString() + "(" + diffString.toString() + ")";
    }
}
