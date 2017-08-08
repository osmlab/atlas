package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag for boundary nodes that are created at boundaries.
 * <p>
 * This is not an OSM tag.
 *
 * @author matthieun
 */
@Tag(synthetic = true)
public enum SyntheticBoundaryNodeTag
{
    YES,
    EXISTING;

    @TagKey
    public static final String KEY = "synthetic_boundary_node";

    private static EnumSet<SyntheticBoundaryNodeTag> ALL_BOUNDARY_NODES = EnumSet.of(YES, EXISTING);
    private static EnumSet<SyntheticBoundaryNodeTag> SYNTHETIC_BOUNDARY_NODES = EnumSet.of(YES);
    private static EnumSet<SyntheticBoundaryNodeTag> EXISTING_BOUNDARY_NODES = EnumSet.of(EXISTING);

    public static boolean isBoundaryNode(final Taggable taggable)
    {
        final Optional<SyntheticBoundaryNodeTag> boundary = Validators
                .from(SyntheticBoundaryNodeTag.class, taggable);
        return boundary.isPresent() && ALL_BOUNDARY_NODES.contains(boundary.get());
    }

    public static boolean isExistingBoundaryNode(final Taggable taggable)
    {
        final Optional<SyntheticBoundaryNodeTag> boundary = Validators
                .from(SyntheticBoundaryNodeTag.class, taggable);
        return boundary.isPresent() && EXISTING_BOUNDARY_NODES.contains(boundary.get());
    }

    public static boolean isSyntheticBoundaryNode(final Taggable taggable)
    {
        final Optional<SyntheticBoundaryNodeTag> boundary = Validators
                .from(SyntheticBoundaryNodeTag.class, taggable);
        return boundary.isPresent() && SYNTHETIC_BOUNDARY_NODES.contains(boundary.get());
    }
}
