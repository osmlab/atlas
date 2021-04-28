package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.slf4j.Logger;

/**
 * @author Sid
 */
public abstract class ComplexWaterEntity extends ComplexEntity
{
    private static final long serialVersionUID = 7835788819725148174L;

    private final WaterType waterType;

    public ComplexWaterEntity(final AtlasEntity source, final WaterType waterType)
    {
        super(source);
        this.waterType = waterType;
        try
        {
            populateGeometry();
        }
        catch (final Exception e)
        {
            getLogger().warn("Unable to create complex water entity from {}", source, e);
            setInvalidReason("Unable to create complex water entity", e);
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof ComplexWaterEntity)
        {
            final ComplexWaterEntity that = (ComplexWaterEntity) other;
            return new EqualsBuilder().append(this.waterType, that.waterType)
                    .append(this.getSource(), that.getSource()).build();
        }
        return false;
    }

    public WaterType getWaterType()
    {
        return this.waterType;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.getSource()).append(this.waterType).build();
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + " " + this.getWaterType() + " " + getSource();
    }

    protected abstract Logger getLogger();

    /**
     * This function populates the geometry
     */
    protected abstract void populateGeometry();
}
