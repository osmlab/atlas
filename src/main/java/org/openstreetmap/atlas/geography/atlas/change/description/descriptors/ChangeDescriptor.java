package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A basic change unit, which when grouped together form a {@link ChangeDescription}.
 * 
 * @author lcram
 */
public interface ChangeDescriptor
{
    ChangeDescriptorType getChangeDescriptorType();

    ChangeDescriptorName getName();

    default JsonElement toJsonElement()
    {
        final JsonObject descriptor = new JsonObject();
        descriptor.addProperty("name", getName().toString());
        descriptor.addProperty("type", getChangeDescriptorType().toString());
        return descriptor;
    }
}
