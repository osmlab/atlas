package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class ChangeLine extends Line // NOSONAR
{
    private static final long serialVersionUID = -5658471275390043045L;

    // At most one of those two can be null. Not using Optional here as it is not Serializable.
    private final Line source;
    private final Line override;

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
        return attribute(Line::relations).stream()
                .map(relation -> getChangeAtlas().relation(relation.getIdentifier()))
                .collect(Collectors.toSet());
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
