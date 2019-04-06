package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Line} that references a {@link ChangeAtlas}. That {@link Line} makes sure that all the
 * parent {@link Relation}s are {@link ChangeRelation}s.
 * <p>
 * NOSONAR here to avoid "Subclasses that add fields should override "equals" (squid:S2160)". Here
 * the equals from the parent works.
 *
 * @author matthieun
 */
public class ChangeLine extends Line // NOSONAR
{
    private static final long serialVersionUID = -5658471275390043045L;

    // At most one of those two can be null. Not using Optional here as it is not Serializable.
    private final Line source;
    private final Line override;

    // Computing Parent Relations is very expensive, so we cache it here.
    private transient Set<Relation> relationsCache;
    private transient Object relationsCacheLock = new Object();

    protected ChangeLine(final ChangeAtlas atlas, final Line source, final Line override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return attribute(Line::asPolyLine);
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Line::getIdentifier);
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Line::getTags);
    }

    @Override
    public Set<Relation> relations()
    {
        Set<Relation> localRelations = this.relationsCache;
        if (localRelations == null)
        {
            synchronized (this.relationsCacheLock)
            {
                localRelations = this.relationsCache;
                if (localRelations == null)
                {
                    localRelations = ChangeEntity.filterRelations(attribute(AtlasEntity::relations),
                            getChangeAtlas());
                    this.relationsCache = localRelations;
                }
            }
        }
        return localRelations;
    }

    private <T extends Object> T attribute(final Function<Line, T> memberExtractor)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
