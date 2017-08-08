package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.Map;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;

/**
 * A primitive object for {@link RelationBean}
 *
 * @author tony
 */
public class AtlasPrimitiveRelation extends AtlasPrimitiveEntity
{
    private static final long serialVersionUID = 4189537752256202439L;
    private final long osmIdentifier;
    private final RelationBean relationBean;
    private final Rectangle bounds;

    public AtlasPrimitiveRelation(final long identifier, final long osmIdentifier,
            final RelationBean relationBean, final Map<String, String> tags, final Rectangle bounds)
    {
        super(identifier, tags);
        this.osmIdentifier = osmIdentifier;
        this.relationBean = relationBean;
        this.bounds = bounds;
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    public long getOsmIdentifier()
    {
        return this.osmIdentifier;
    }

    public RelationBean getRelationBean()
    {
        return this.relationBean;
    }
}
