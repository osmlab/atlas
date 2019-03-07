package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Type of item in an {@link Atlas}
 *
 * @author matthieun
 */
public enum ItemType
{
    NODE(0)
    {
        @Override
        public Iterable<Node> entitiesForIdentifiers(final Atlas atlas, final Long... identifiers)
        {
            return atlas.nodes(identifiers);
        }
    },
    EDGE(1)
    {
        @Override
        public Iterable<Edge> entitiesForIdentifiers(final Atlas atlas, final Long... identifiers)
        {
            return atlas.edges(identifiers);
        }
    },
    AREA(2)
    {
        @Override
        public Iterable<Area> entitiesForIdentifiers(final Atlas atlas, final Long... identifiers)
        {
            return atlas.areas(identifiers);
        }
    },
    LINE(3)
    {
        @Override
        public Iterable<Line> entitiesForIdentifiers(final Atlas atlas, final Long... identifiers)
        {
            return atlas.lines(identifiers);
        }
    },
    POINT(4)
    {
        @Override
        public Iterable<Point> entitiesForIdentifiers(final Atlas atlas, final Long... identifiers)
        {
            return atlas.points(identifiers);
        }
    },
    RELATION(5)
    {
        @Override
        public Iterable<Relation> entitiesForIdentifiers(final Atlas atlas,
                final Long... identifiers)
        {
            return atlas.relations(identifiers);
        }
    };

    private final int value;

    public static ItemType forEntity(final AtlasEntity entity)
    {
        return entity.getType();
    }

    public static ItemType forValue(final int value)
    {
        for (final ItemType type : values())
        {
            if (type.getValue() == value)
            {
                return type;
            }
        }
        throw new CoreException("Invalid value: {}", value);
    }

    public static ItemType shortValueOf(final String value)
    {
        switch (value)
        {
            case "N":
                return NODE;
            case "E":
                return EDGE;
            case "A":
                return AREA;
            case "L":
                return LINE;
            case "P":
                return POINT;
            case "R":
                return RELATION;
            default:
                throw new CoreException("Invalid short value {}", value);
        }
    }

    ItemType(final int value)
    {
        this.value = value;
    }

    public AtlasEntity entityForIdentifier(final Atlas atlas, final long identifier)
    {
        switch (this)
        {
            case NODE:
                return atlas.node(identifier);
            case EDGE:
                return atlas.edge(identifier);
            case AREA:
                return atlas.area(identifier);
            case LINE:
                return atlas.line(identifier);
            case POINT:
                return atlas.point(identifier);
            case RELATION:
                return atlas.relation(identifier);

            default:
                throw new CoreException("Invalid type {}", this);
        }
    }

    public abstract <E extends AtlasEntity> Iterable<E> entitiesForIdentifiers(Atlas atlas,
            Long... identifiers);

    @SuppressWarnings("unchecked")
    public <M extends AtlasEntity> Class<M> getMemberClass()
    {
        switch (this)
        {
            case NODE:
                return (Class<M>) Node.class;
            case EDGE:
                return (Class<M>) Edge.class;
            case AREA:
                return (Class<M>) Area.class;
            case LINE:
                return (Class<M>) Line.class;
            case POINT:
                return (Class<M>) Point.class;
            case RELATION:
                return (Class<M>) Relation.class;

            default:
                throw new CoreException("Invalid type {}", this);
        }
    }

    public int getValue()
    {
        return this.value;
    }

    public String toShortString()
    {
        switch (this)
        {
            case NODE:
                return "N";
            case EDGE:
                return "E";
            case AREA:
                return "A";
            case LINE:
                return "L";
            case POINT:
                return "P";
            case RELATION:
                return "R";
            default:
                throw new CoreException("Invalid type {}", this);
        }
    }
}
