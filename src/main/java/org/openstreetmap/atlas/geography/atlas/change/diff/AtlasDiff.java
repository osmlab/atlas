package org.openstreetmap.atlas.geography.atlas.change.diff;

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
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class AtlasDiff
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDiff.class);

    private final Atlas before;
    private final Atlas after;
    private boolean geometryMatching = false;
    private boolean saveAllGeometries = false;

    /**
     * Construct a diff that will change the before {@link Atlas} into the after {@link Atlas}.
     *
     * @param before
     *            the initial {@link Atlas}
     * @param after
     *            the changed {@link Atlas}
     */
    public AtlasDiff(final Atlas before, final Atlas after)
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
        for (final AtlasEntity beforeEntity : this.before)
        {
            final Long beforeEntityIdentifier = beforeEntity.getIdentifier();
            /*
             * Look up the beforeEntity ID in the after atlas. If the entity is missing, we know it
             * was removed from the after atlas.
             */
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

        for (final AtlasEntity afterEntity : this.after)
        {
            final Long afterEntityIdentifier = afterEntity.getIdentifier();
            /*
             * Look up the afterEntity ID in the before atlas. If the entity is missing, we know it
             * was added to the after atlas.
             */
            if (afterEntity.getType().entityForIdentifier(this.before,
                    afterEntity.getIdentifier()) == null)
            {
                FeatureChange featureChange;
                switch (afterEntity.getType())
                {
                    case NODE:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedNode.fromNode(this.after.node(afterEntityIdentifier)));
                        break;
                    case EDGE:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedEdge.fromEdge(this.after.edge(afterEntityIdentifier)));
                        break;
                    case POINT:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedPoint.fromPoint(this.after.point(afterEntityIdentifier)));
                        break;
                    case LINE:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedLine.fromLine(this.after.line(afterEntityIdentifier)));
                        break;
                    case AREA:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedArea.fromArea(this.after.area(afterEntityIdentifier)));
                        break;
                    case RELATION:
                        featureChange = new FeatureChange(ChangeType.ADD, BloatedRelation
                                .fromRelation(this.after.relation(afterEntityIdentifier)));
                        break;
                    default:
                        throw new CoreException("Unknown item type {}", afterEntity.getType());
                }
                changeBuilder.add(featureChange);
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

    public AtlasDiff saveAllGeometries(final boolean saveAllGeometries)
    {
        this.saveAllGeometries = saveAllGeometries;
        return this;
    }

    public AtlasDiff withGeometryMatching(final boolean matching)
    {
        this.geometryMatching = matching;
        return this;
    }
}
