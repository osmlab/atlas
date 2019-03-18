package org.openstreetmap.atlas.utilities.graphs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.maps.LinkedMultiMap;

/**
 * A directed acyclic graph. See http://en.wikipedia.org/wiki/Directed_acyclic_graph
 *
 * @param <V>
 *            The objects used in the graph
 * @author cuthbertm
 * @author mgostintsev
 */
public class DirectedAcyclicGraph<V> implements Serializable
{
    private static final long serialVersionUID = -1980678458375645486L;

    private final LinkedMultiMap<V, V> inMap = new LinkedMultiMap<>();
    private final LinkedMultiMap<V, V> outMap = new LinkedMultiMap<>();

    public boolean addEdge(final V origin, final V target)
    {
        if (origin == null || target == null)
        {
            throw new CoreException(
                    "Origin and Target for directed Acyclic graph must not be null");
        }

        if (hasPath(target, origin))
        {
            return false;
        }

        this.outMap.add(origin, target);
        this.outMap.add(target, null);
        this.inMap.add(target, origin);
        this.inMap.add(origin, null);
        return true;
    }

    public void addVertex(final V vertex)
    {
        if (vertex == null)
        {
            throw new CoreException("Cannot add a null vertex to the Directed Acyclic Graph.");
        }

        this.outMap.put(vertex, null);
        this.inMap.put(vertex, null);
    }

    public boolean contains(final V vertex)
    {
        return this.outMap.containsKey(vertex) || this.inMap.containsKey(vertex);
    }

    public Set<V> getChildren(final V parent)
    {
        return Collections.unmodifiableSet(this.outMap.get(parent));
    }

    public int getDeepestLevel(final V vertex)
    {
        return getDeepestLevel(vertex, 1);
    }

    public Set<V> getParents(final V child)
    {
        return Collections.unmodifiableSet(this.inMap.get(child));
    }

    public Set<V> getSinks()
    {
        return getZeroEdgeVertices(this.outMap);
    }

    public Set<V> getSources()
    {
        return getZeroEdgeVertices(this.inMap);
    }

    /**
     * A DFS topological sort
     *
     * @return a topologically sorted stack
     */
    public Stack<V> getTopologicalSortedList()
    {
        final Stack<V> stack = new Stack<>();
        final Set<V> visited = new HashSet<>();
        this.inMap.forEach((inVertex, outVertex) ->
        {
            if (!visited.contains(inVertex))
            {
                topologicalSort(inVertex, visited, stack);
            }
        });

        return stack;
    }

    public boolean hasPath(final V start, final V end)
    {
        if (start == end)
        {
            return true;
        }

        final Set<V> children = this.outMap.get(start);
        final Iterator<V> iterator = children.iterator();
        while (iterator.hasNext())
        {
            if (hasPath(iterator.next(), end))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isSink(final V vertex)
    {
        return this.outMap.get(vertex).isEmpty();
    }

    public boolean isSource(final V vertex)
    {
        return this.inMap.get(vertex).isEmpty();
    }

    /**
     * @return The ordered groups of vertices that all belong to the same priority level within the
     *         DAG
     */
    public List<Set<V>> processGroups()
    {
        final Stack<Set<V>> stack = new Stack<>();
        stack.push(new HashSet<>(getSinks()));
        final Set<V> added = new HashSet<>(getSinks());
        final Set<V> sourcesNotAdded = new HashSet<>(getSources());
        while (!sourcesNotAdded.isEmpty())
        {
            final Set<V> allCandidates = new HashSet<>();
            final Set<V> candidates = new HashSet<>();
            for (final V alreadyAdded : added)
            {
                for (final V parent : getParents(alreadyAdded))
                {
                    if (!added.contains(parent))
                    {
                        allCandidates.add(parent);
                    }
                }
            }
            for (final V candidate : allCandidates)
            {
                if (added.containsAll(getChildren(candidate)))
                {
                    candidates.add(candidate);
                    // Hit or miss, hit only at the end of the processing
                    sourcesNotAdded.remove(candidate);
                }
            }
            stack.push(candidates);
            added.addAll(candidates);
        }
        final List<Set<V>> result = new ArrayList<>();
        while (!stack.isEmpty())
        {
            result.add(stack.pop());
        }
        return result;
    }

    public void removeVertex(final V vertex)
    {
        final Set<V> targets = this.outMap.remove(vertex);
        if (targets != null)
        {
            targets.forEach(target -> this.outMap.remove(target, vertex));
        }
        final Set<V> origins = this.inMap.remove(vertex);
        if (origins != null)
        {
            origins.forEach(origin -> this.inMap.remove(origin, vertex));
        }
    }

    @Override
    public String toString()
    {
        return "[Out: " + this.outMap.toString() + ", In: " + this.inMap.toString() + "]";
    }

    private int getDeepestLevel(final V vertex, final int level)
    {
        if (isSource(vertex))
        {
            return level;
        }
        final Set<V> parents = getParents(vertex);
        int nextLevel = level;
        for (final V parent : parents)
        {
            nextLevel = Math.max(nextLevel, getDeepestLevel(parent, level) + 1);
        }
        return nextLevel;
    }

    private Set<V> getZeroEdgeVertices(final LinkedMultiMap<V, V> map)
    {
        final Set<V> mapKeys = map.keySet();
        final Set<V> zeroEdges = new LinkedHashSet<>(mapKeys.size());
        mapKeys.stream().filter(key -> map.get(key).isEmpty()).forEach(key -> zeroEdges.add(key));
        return zeroEdges;
    }

    private void topologicalSort(final V current, final Set<V> visited, final Stack<V> stack)
    {
        visited.add(current);
        getChildren(current).iterator().forEachRemaining(child ->
        {
            if (!visited.contains(child))
            {
                topologicalSort(child, visited, stack);
            }
        });
        stack.push(current);
    }
}
