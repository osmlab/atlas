package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ChangeAtlasTest
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeAtlasTest.class);

    @Rule
    public final ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Test
    public void generalTestChangedEntities()
    {
        final org.openstreetmap.atlas.geography.atlas.items.Node node = this.rule.getAtlas()
                .node(3L);
        final BloatedNode bloatedNode = BloatedNode.shallowFromNode(node)
                .withTags(Maps.stringMap("tag1", "value1"));
        final FeatureChange featureChange = new FeatureChange(ChangeType.REMOVE, bloatedNode);
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        changeBuilder.add(featureChange);
        final ChangeAtlas changeAtlas = new ChangeAtlas(this.rule.getAtlas(), changeBuilder.get());
        for (final AtlasEntity entity : changeAtlas.nodes())
        {
            logger.warn("ID {}: {}", entity.getIdentifier(), entity.getTags());
        }
    }
}
