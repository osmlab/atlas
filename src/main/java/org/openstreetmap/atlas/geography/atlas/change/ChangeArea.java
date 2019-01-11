package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Area} that references a {@link ChangeAtlas}. That {@link Area} makes sure that all the
 * parent {@link Relation}s are {@link ChangeRelation}s.
 * <p>
 * NOSONAR here to avoid "Subclasses that add fields should override "equals" (squid:S2160)". Here
 * the equals from the parent works.
 *
 * @author matthieun
 */
public class ChangeArea extends Area // NOSONAR
{
    private static final long serialVersionUID = -5658471275390043045L;

    // At most one of those two can be null. Not using Optional here as it is not Serializable.
    private final Area source;
    private final Area override;

    protected ChangeArea(final ChangeAtlas atlas, final Area source, final Area override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public Polygon asPolygon()
    {
        return attribute(Area::asPolygon);
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Area::getIdentifier);
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Area::getTags);
    }

    @Override
    public Set<Relation> relations()
    {
        return ChangeEntity.filterRelations(attribute(AtlasEntity::relations), getChangeAtlas());
    }

    private <T extends Object> T attribute(final Function<Area, T> memberExtractor)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
