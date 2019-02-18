package org.openstreetmap.atlas.geography.atlas.complete;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;

/**
 * Similar to a {@link org.openstreetmap.atlas.geography.atlas.items.LineItem} but for
 * {@link CompleteEntity}-ies.
 *
 * @author Yazad Khambata
 */
public interface CompleteLineItem extends CompleteEntity
{
    CompleteLineItem withPolyLine(PolyLine polyLine);

    CompleteLineItem withAggregateBoundsExtendedUsing(Rectangle bounds);
}
