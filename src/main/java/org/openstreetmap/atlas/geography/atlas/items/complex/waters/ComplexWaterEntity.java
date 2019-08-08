package org.openstreetmap.atlas.geography.atlas.items.complex.waters;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.slf4j.Logger;

/**
 * @author Sid
 */
public abstract class ComplexWaterEntity extends ComplexEntity
{
    private static final long serialVersionUID = 7835788819725148174L;

    private final String waterType;

    public ComplexWaterEntity(final AtlasEntity source, final String waterType)
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

    public String getWaterType()
    {
        return this.waterType;
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
