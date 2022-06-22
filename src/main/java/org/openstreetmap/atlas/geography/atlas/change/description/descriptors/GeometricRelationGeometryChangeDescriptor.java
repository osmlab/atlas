package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import java.util.Optional;

import org.locationtech.jts.geom.MultiPolygon;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author samg
 */
public final class GeometricRelationGeometryChangeDescriptor implements ChangeDescriptor
{

    private static final Logger logger = LoggerFactory
            .getLogger(GeometricRelationGeometryChangeDescriptor.class);
    private final ChangeDescriptorType changeType;
    private final String diff;

    public static GeometricRelationGeometryChangeDescriptor getDescriptorsForGeometricRelations(
            final Relation before, final Relation after)
    {
        if (before == null)
        {
            if (after == null)
            {
                return null;
            }
            final Optional<MultiPolygon> afterGeometry = after.asMultiPolygon();
            if (afterGeometry.isPresent())
            {
                return new GeometricRelationGeometryChangeDescriptor(afterGeometry.get().toText(),
                        ChangeDescriptorType.ADD);
            }
            return null;
        }
        else if (after == null)
        {
            final Optional<MultiPolygon> beforeGeometry = before.asMultiPolygon();
            if (beforeGeometry.isPresent())
            {
                return new GeometricRelationGeometryChangeDescriptor(beforeGeometry.get().toText(),
                        ChangeDescriptorType.REMOVE);
            }
            return null;
        }
        final Optional<MultiPolygon> beforeGeometry = before.asMultiPolygon();
        final Optional<MultiPolygon> afterGeometry = after.asMultiPolygon();
        if (beforeGeometry.isEmpty() && afterGeometry.isEmpty())
        {
            return null;
        }
        else if (beforeGeometry.isPresent() && afterGeometry.isEmpty())
        {
            return new GeometricRelationGeometryChangeDescriptor(beforeGeometry.get().toText(),
                    ChangeDescriptorType.REMOVE);
        }
        else if (beforeGeometry.isEmpty() && afterGeometry.isPresent())
        {
            return new GeometricRelationGeometryChangeDescriptor(afterGeometry.get().toText(),
                    ChangeDescriptorType.ADD);
        }
        try
        {
            if (beforeGeometry.get().equals(afterGeometry.get()))
            {
                return null;
            }
        }
        catch (final Exception exc)
        {
            logger.error("Geometry equals failed for relation {}", before.getIdentifier(), exc);
        }

        try
        {
            if (beforeGeometry.get().getArea() == afterGeometry.get().getArea())
            {
                return null;
            }
            return new GeometricRelationGeometryChangeDescriptor(
                    Double.toString(beforeGeometry.get().getArea() - afterGeometry.get().getArea()),
                    ChangeDescriptorType.UPDATE);
        }
        catch (final Exception exc)
        {
            logger.error("Geometry intersection failed for relation {}", before.getIdentifier(),
                    exc);
            throw new CoreException("Couldn't calculate diff for relation {}",
                    before.getIdentifier());
        }
    }

    private GeometricRelationGeometryChangeDescriptor(final String diff,
            final ChangeDescriptorType changeType)
    {
        this.changeType = changeType;
        this.diff = diff;
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    @Override
    public ChangeDescriptorName getName()
    {
        return ChangeDescriptorName.GEOMETRY;
    }

    @Override
    public JsonElement toJsonElement()
    {
        final JsonObject descriptor = (JsonObject) ChangeDescriptor.super.toJsonElement();
        descriptor.addProperty("diff", this.diff);
        return descriptor;
    }

    @Override
    public String toString()
    {
        final StringBuilder diffString = new StringBuilder();
        diffString.append(this.changeType.toString());
        diffString.append(", ");
        switch (this.changeType)
        {
            case UPDATE:
                diffString.append(", ");
                diffString.append(this.diff);
                break;
            case REMOVE:
                diffString.append(", ");
                diffString.append(this.diff);
                break;
            case ADD:
                diffString.append(", ");
                diffString.append(this.diff);
                break;
            default:
                throw new CoreException("Unexpected ChangeType value: " + this.changeType);
        }
        return getName().toString() + "(" + diffString.toString() + ")";
    }
}
