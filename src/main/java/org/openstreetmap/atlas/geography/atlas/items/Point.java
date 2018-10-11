package org.openstreetmap.atlas.geography.atlas.items;

import com.google.gson.JsonObject;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * A Point that is not navigable.
 *
 * @author matthieun
 */
public abstract class Point extends LocationItem
{
    private static final long serialVersionUID = -7888952319754555424L;

    protected Point(final Atlas atlas)
    {
        super(atlas);
    }

    @Override
    public ItemType getType()
    {
        return ItemType.POINT;
    }

    @Override
    public String toDiffViewFriendlyString()
    {
        final String relationsString = this.parentRelationsAsDiffViewFriendlyString();

        return "[Point: id=" + this.getIdentifier() + ", location=" + this.getLocation()
                + ", relations=(" + relationsString + "), " + tagString() + "]";
    }

    @Override
    public String toString()
    {
        return "[Point: id=" + this.getIdentifier() + ", location=" + this.getLocation() + ", "
                + tagString() + "]";
    }

    @Override
    public JsonObject asGeoJsonFeature()
    {
        final JsonObject jsonObject = super.asGeoJsonFeature();

        jsonObject.addProperty("point", "helloworld");

        return jsonObject;
    }
}
