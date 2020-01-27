package org.openstreetmap.atlas.geography.clipping;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to compute geometry clips
 *
 * @author matthieun
 * @deprecated Use {@link GeometryOperation} instead.
 */
@Deprecated
public class Clip
{
    /**
     * Type of clip
     *
     * @author matthieun
     */
    public enum ClipType
    {
        AND,
        OR,
        NOT,
        XOR
    }

    private static final Logger logger = LoggerFactory.getLogger(Clip.class);

    private final ClipType clipType;
    private final List<? extends PolyLine> clip;
    private final MultiPolygon clipMulti;

    public Clip(final ClipType clipType, final MultiPolygon subject, final MultiPolygon clipping)
    {
        this.clipType = clipType;
        this.clip = null;
        this.clipMulti = clip(subject, clipping);
    }

    public Clip(final ClipType clipType, final PolyLine subject, final MultiPolygon clipping)
    {
        this.clipType = clipType;
        if (subject instanceof Polygon)
        {
            this.clipMulti = clip((Polygon) subject, clipping);
            this.clip = null;
        }
        else
        {
            this.clipMulti = null;
            this.clip = clip(subject, clipping);
        }
    }

    public Clip(final ClipType clipType, final PolyLine subject, final Polygon clipping)
    {
        this.clipType = clipType;
        if (subject instanceof Polygon)
        {
            this.clip = clip((Polygon) subject, clipping);
        }
        else
        {
            this.clip = clip(subject, clipping);
        }
        this.clipMulti = null;
    }

    public List<? extends PolyLine> getClip()
    {
        return this.clip;
    }

    public MultiPolygon getClipMultiPolygon()
    {
        return this.clipMulti;
    }

    public boolean returnsMultiPolygon()
    {
        return this.clipMulti != null;
    }

    public boolean returnsPolygonOrPolyLineList()
    {
        return this.clip != null;
    }

    private MultiPolygon clip(final MultiPolygon subject, final MultiPolygon clipping)
    {
        switch (this.clipType)
        {
            case AND:
                return new MultiPolygonClipper(clipping).and(subject);
            case OR:
                return new MultiPolygonClipper(clipping).union(subject);
            case NOT:
                return new MultiPolygonClipper(clipping).not(subject);
            case XOR:
                return new MultiPolygonClipper(clipping).xor(subject);
            default:
                throw new CoreException("Invalid Clip Type.");
        }
    }

    private MultiPolygon clip(final Polygon subject, final MultiPolygon clipping)
    {
        switch (this.clipType)
        {
            case AND:
                return new MultiPolygonClipper(clipping).and(subject);
            case OR:
                return new MultiPolygonClipper(clipping).union(subject);
            case NOT:
                return new MultiPolygonClipper(clipping).not(subject);
            case XOR:
                return new MultiPolygonClipper(clipping).xor(subject);
            default:
                throw new CoreException("Invalid Clip Type.");
        }
    }

    private List<? extends PolyLine> clip(final Polygon subject, final Polygon clipping)
    {
        switch (this.clipType)
        {
            case AND:
                return toPolygons(new PolygonClipper(clipping).and(subject));
            case OR:
                return toPolygons(new PolygonClipper(clipping).union(subject));
            case NOT:
                return toPolygons(new PolygonClipper(clipping).not(subject));
            case XOR:
                return toPolygons(new PolygonClipper(clipping).xor(subject));
            default:
                throw new CoreException("Invalid Clip Type.");
        }
    }

    private List<? extends PolyLine> clip(final PolyLine subject, final MultiPolygon clipping)
    {
        switch (this.clipType)
        {
            case AND:
                return new MultiPolygonClipper(clipping).and(subject);
            case OR:
                return new MultiPolygonClipper(clipping).union(subject);
            case NOT:
                return new MultiPolygonClipper(clipping).not(subject);
            case XOR:
                return new MultiPolygonClipper(clipping).xor(subject);
            default:
                throw new CoreException("Invalid Clip Type.");
        }
    }

    private List<? extends PolyLine> clip(final PolyLine subject, final Polygon clipping)
    {
        switch (this.clipType)
        {
            case AND:
                return new PolygonClipper(clipping).and(subject);
            case OR:
                return new PolygonClipper(clipping).union(subject);
            case NOT:
                return new PolygonClipper(clipping).not(subject);
            case XOR:
                return new PolygonClipper(clipping).xor(subject);
            default:
                throw new CoreException("Invalid Clip Type.");
        }
    }

    private List<? extends PolyLine> toPolygons(final Iterable<? extends PolyLine> input)
    {
        final Iterable<PolyLine> iterables = Iterables.translate(input, polyLine ->
        {
            if (polyLine != null && !(polyLine instanceof Polygon))
            {
                logger.warn("Something is not a Polygon {} : {}", polyLine.getClass(), polyLine);
                // throw new CoreException("Something is not a Polygon...");
            }
            return polyLine instanceof Polygon ? polyLine : null;
        });
        // We filter off anything thats not a polygon
        return Iterables.asList(Iterables.filter(iterables, p -> p != null));
    }
}
