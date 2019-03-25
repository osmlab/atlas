package org.openstreetmap.atlas.utilities.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * Basic validation of DAG functionality
 *
 * @author cuthbertm
 * @author mgostintsev
 */
public class DirectedAcyclicGraphTest
{
    private final DirectedAcyclicGraph<String> graph;

    public DirectedAcyclicGraphTest()
    {
        this.graph = new DirectedAcyclicGraph<>();
    }

    @Before
    public void setup()
    {
        // Vertices
        this.graph.addVertex("Platinum");
        this.graph.addVertex("Gold");
        this.graph.addVertex("Copper");
        this.graph.addVertex("Precious");
        this.graph.addVertex("Coinage");
        this.graph.addVertex("Metal");

        // Edges
        this.graph.addEdge("Metal", "Coinage");
        this.graph.addEdge("Metal", "Precious");
        this.graph.addEdge("Precious", "Platinum");
        this.graph.addEdge("Precious", "Gold");
        this.graph.addEdge("Coinage", "Gold");
        this.graph.addEdge("Coinage", "Copper");
    }

    @Test
    public void testDeepestLevel()
    {
        assertEquals(this.graph.getDeepestLevel("Metal"), 1);
        assertEquals(this.graph.getDeepestLevel("Gold"), 3);
    }

    @Test
    public void testPaths()
    {
        assertTrue(this.graph.hasPath("Metal", "Copper"));
        assertTrue(this.graph.hasPath("Metal", "Platinum"));
        assertFalse(this.graph.hasPath("Coinage", "Platinum"));

        // Adding this edge will cause a cycle, which is not allowed in a DAG
        Assert.assertTrue(!this.graph.addEdge("Copper", "Metal"));
    }

    @Test
    public void testProcessGroups()
    {
        final List<Set<String>> processGroups = this.graph.processGroups();
        final List<Set<String>> expected = new ArrayList<>();
        final Set<String> first = Sets.hashSet("Metal");
        final Set<String> second = Sets.hashSet("Precious", "Coinage");
        final Set<String> third = Sets.hashSet("Gold", "Platinum", "Copper");
        expected.add(first);
        expected.add(second);
        expected.add(third);
        Assert.assertEquals(expected, processGroups);
    }

    @Test
    public void testSinks()
    {
        final Set<String> expectedSinks = new HashSet<>(
                Arrays.asList("Platinum", "Gold", "Copper"));
        assertTrue(this.graph.getSinks().stream().allMatch(sink -> expectedSinks.remove(sink))
                && expectedSinks.isEmpty());
    }

    @Test
    public void testSources()
    {
        final Set<String> expectedSources = new HashSet<>(Arrays.asList("Metal"));
        assertTrue(
                this.graph.getSources().stream().allMatch(source -> expectedSources.remove(source))
                        && expectedSources.isEmpty());
    }

    @Test
    public void testTopologicalSort()
    {
        final Stack<String> topologicalSort = this.graph.getTopologicalSortedList();
        Assert.assertEquals("Metal", topologicalSort.pop());
        Assert.assertEquals("Coinage", topologicalSort.pop());
        Assert.assertEquals("Precious", topologicalSort.pop());
        Assert.assertEquals("Copper", topologicalSort.pop());
        Assert.assertEquals("Gold", topologicalSort.pop());
        Assert.assertEquals("Platinum", topologicalSort.pop());
    }
}
