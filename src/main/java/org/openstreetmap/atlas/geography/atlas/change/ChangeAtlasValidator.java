package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;

/**
 * @author matthieun
 */
public class ChangeAtlasValidator
{
    private final ChangeAtlas atlas;

    protected ChangeAtlasValidator(final ChangeAtlas atlas)
    {
        this.atlas = atlas;
    }

    protected void validate()
    {
        System.out.println("ChangeAtlas Validation??");
        validateEdgeNodeConnectivity();
    }

    private void validateEdgeNodeConnectivity()
    {
        for (final Edge edge : this.atlas.edges())
        {
            // TODO Check edge is properly connected to nodes at the end locations
        }
        for (final Node node : this.atlas.nodes())
        {
            // TODO Check node is properly connected to edges at the same location
        }
    }
}
