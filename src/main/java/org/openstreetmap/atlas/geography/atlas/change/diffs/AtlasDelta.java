package org.openstreetmap.atlas.geography.atlas.change.diffs;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class AtlasDelta
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDelta.class);

    private final Atlas before;
    private final Atlas after;

    public AtlasDelta(final Atlas before, final Atlas after)
    {
        this.before = before;
        this.after = after;
    }

    /**
     * Generate a {@link Change} such that the {@link ChangeAtlas} produced by combining this
     * {@link Change} with the before {@link Atlas} would be effectively equivalent to the after
     * {@link Atlas}.
     *
     * @return the change
     */
    public Change generateChange()
    {
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        // Check for removed entities
        logger.info("Looking for removed entities.");
        for (final AtlasEntity beforeEntity : this.before)
        {
            final Long beforeEntityIdentifier = beforeEntity.getIdentifier();
            if (beforeEntity.getType().entityForIdentifier(this.after,
                    beforeEntity.getIdentifier()) == null)
            {
                FeatureChange featureChange;
                switch (beforeEntity.getType())
                {
                    case NODE:
                        featureChange = new FeatureChange(ChangeType.REMOVE, BloatedNode
                                .shallowFromNode(this.before.node(beforeEntityIdentifier)));
                        break;
                    case EDGE:
                        featureChange = new FeatureChange(ChangeType.REMOVE, BloatedEdge
                                .shallowFromEdge(this.before.edge(beforeEntityIdentifier)));
                        break;
                    case POINT:
                        featureChange = new FeatureChange(ChangeType.REMOVE, BloatedPoint
                                .shallowFromPoint(this.before.point(beforeEntityIdentifier)));
                        break;
                    case LINE:
                        featureChange = new FeatureChange(ChangeType.REMOVE, BloatedLine
                                .shallowFromLine(this.before.line(beforeEntityIdentifier)));
                        break;
                    case AREA:
                        featureChange = new FeatureChange(ChangeType.REMOVE, BloatedArea
                                .shallowFromArea(this.before.area(beforeEntityIdentifier)));
                        break;
                    case RELATION:
                        featureChange = new FeatureChange(ChangeType.REMOVE, BloatedRelation
                                .shallowFromRelation(this.before.relation(beforeEntityIdentifier)));
                        break;
                    default:
                        throw new CoreException("Unknown item type {}", beforeEntity.getType());
                }
                changeBuilder.add(featureChange);
            }
        }

        // Check for added entities
        logger.info("Looking for added entities.");
        for (final AtlasEntity afterEntity : this.after)
        {
            final Long afterEntityIdentifier = afterEntity.getIdentifier();
            if (afterEntity.getType().entityForIdentifier(this.before,
                    afterEntity.getIdentifier()) == null)
            {
                // this.differences.add(new Diff(entity.getType(), DiffType.ADDED, DiffReason.ADDED,
                // this.before, this.after, entity.getIdentifier()));
            }
        }

        return changeBuilder.get();
    }

    public Atlas getAfterAtlas()
    {
        return this.after;
    }

    public Atlas getBeforeAtlas()
    {
        return this.before;
    }
}
