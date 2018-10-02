package org.openstreetmap.atlas.tags.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * {@link Taggable} filter that relies on a String definition
 * <p>
 * Examples of String definition:
 * <p>
 * highway=motorway AND name=[not empty] <br>
 * highway-&gt;motorway&amp;name-&gt;*
 * <p>
 * highway=motorway OR oneway=[not]yes <br>
 * highway-&gt;motorway|oneway-&gt;!yes
 * <p>
 * highway=motorway OR [no "name" tag] <br>
 * highway-&gt;motorway|name-&gt;!
 * <p>
 * amenity=bus_station OR highway=bus_stop OR ( (bus=* OR trolleybus=*) AND
 * public_transport=[stop_position OR platform OR station] ) <br>
 * amenity-&gt;bus_station|highway-&gt;bus_stop|bus-&gt;*||trolleybus-&gt;*&amp;public_transport-&gt;
 * stop_position, platform,station
 *
 * @author matthieun
 */
public class TaggableFilter implements Predicate<Taggable>, Serializable
{
    /**
     * @author matthieun
     */
    protected enum TreeBoolean
    {
        AND,
        OR;

        public TreeBoolean other()
        {
            switch (this)
            {
                case AND:
                    return OR;
                case OR:
                    return AND;
                default:
                    throw new CoreException(ERROR_MESSAGE, this);
            }
        }

        public String separator()
        {
            switch (this)
            {
                case AND:
                    return "&";
                case OR:
                    return "|";
                default:
                    throw new CoreException(ERROR_MESSAGE, this);
            }
        }
    }

    private static final long serialVersionUID = 5697377487014951158L;
    private static final String ERROR_MESSAGE = "Unknown TreeBoolean {}";

    private final List<TaggableFilter> children;
    private final TreeBoolean treeBoolean;
    private final Predicate<Taggable> simple;
    private final String definition;

    public static TaggableFilter forDefinition(final String definition)
    {
        return new LineFilterConverter().convert(definition);
    }

    protected TaggableFilter(final List<TaggableFilter> children, final TreeBoolean treeBoolean)
    {
        this.children = children;
        this.treeBoolean = treeBoolean;
        this.simple = null;
        this.definition = null;
    }

    protected TaggableFilter(final Predicate<Taggable> simple, final String definition)
    {
        this.children = new ArrayList<>();
        this.treeBoolean = TreeBoolean.OR;
        this.simple = simple;
        this.definition = definition;
    }

    @Override
    public boolean test(final Taggable taggable)
    {
        if (this.simple != null)
        {
            return this.simple.test(taggable);
        }
        if (this.children.isEmpty())
        {
            throw new CoreException("Malformed predicate {}", this);
        }
        switch (this.treeBoolean)
        {
            case AND:
                return this.children.stream().allMatch(tree -> tree.test(taggable));
            case OR:
                return this.children.stream().anyMatch(tree -> tree.test(taggable));
            default:
                throw new CoreException(ERROR_MESSAGE, this);
        }
    }

    @Override
    public String toString()
    {
        return new LineFilterConverter().backwardConvert(this);
    }

    protected List<TaggableFilter> getChildren()
    {
        return this.children;
    }

    protected Optional<String> getDefinition()
    {
        return Optional.ofNullable(this.definition);
    }

    protected TreeBoolean getTreeBoolean()
    {
        return this.treeBoolean;
    }
}
