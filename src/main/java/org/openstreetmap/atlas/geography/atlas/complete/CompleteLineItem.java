package org.openstreetmap.atlas.geography.atlas.complete;

import org.openstreetmap.atlas.geography.PolyLine;

/**
 * Similar to a {@link org.openstreetmap.atlas.geography.atlas.items.LineItem} but for
 * {@link CompleteEntity}-ies.
 *
 * @param <E>
 *            the {@link CompleteEntity} being worked on.
 * @author Yazad Khambata
 */
public interface CompleteLineItem<E extends CompleteLineItem<E>> extends CompleteEntity<E>
{
    CompleteLineItem withPolyLine(PolyLine polyLine);
}
