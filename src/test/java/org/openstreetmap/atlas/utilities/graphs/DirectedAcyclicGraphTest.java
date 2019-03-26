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
    private static final String PLATINUM = "Platinum";
    private static final String GOLD = "Gold";
    private static final String COPPER = "Copper";
    private static final String PRECIOUS = "Precious";
    private static final String COINAGE = "Coinage";
    private static final String METAL = "Metal";

    private final DirectedAcyclicGraph<String> graph;

    public DirectedAcyclicGraphTest()
    {
        this.graph = new DirectedAcyclicGraph<>();
    }

    @Before
    public void setup()
    {
        // Vertices
        this.graph.addVertex(PLATINUM);
        this.graph.addVertex(GOLD);
        this.graph.addVertex(COPPER);
        this.graph.addVertex(PRECIOUS);
        this.graph.addVertex(COINAGE);
        this.graph.addVertex(METAL);

        // Edges
        this.graph.addEdge(METAL, COINAGE);
        this.graph.addEdge(METAL, PRECIOUS);
        this.graph.addEdge(PRECIOUS, PLATINUM);
        this.graph.addEdge(PRECIOUS, GOLD);
        this.graph.addEdge(COINAGE, GOLD);
        this.graph.addEdge(COINAGE, COPPER);
    }

    @Test
    public void testDeepestLevel()
    {
        assertEquals(this.graph.getDeepestLevel(METAL), 1);
        assertEquals(this.graph.getDeepestLevel(GOLD), 3);
    }

    @Test
    public void testPaths()
    {
        assertTrue(this.graph.hasPath(METAL, COPPER));
        assertTrue(this.graph.hasPath(METAL, PLATINUM));
        assertFalse(this.graph.hasPath(COINAGE, PLATINUM));

        // Adding this edge will cause a cycle, which is not allowed in a DAG
        Assert.assertTrue(!this.graph.addEdge(COPPER, METAL));
    }

    @Test
    public void testProcessGroups()
    {
        final List<Set<String>> processGroups = this.graph.processGroups();
        final List<Set<String>> expected = new ArrayList<>();
        final Set<String> first = Sets.hashSet(METAL);
        final Set<String> second = Sets.hashSet(PRECIOUS, COINAGE);
        final Set<String> third = Sets.hashSet(GOLD, PLATINUM, COPPER);
        expected.add(first);
        expected.add(second);
        expected.add(third);
        Assert.assertEquals(expected, processGroups);
    }

    @Test
    public void testProcessGroupsWithSourceSinks()
    {
        final DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();
        dag.addVertex("a");
        dag.addVertex("b");
        dag.addVertex("c");
        final List<Set<String>> processGroups = dag.processGroups();
        Assert.assertEquals(1, processGroups.size());
    }

    @Test
    public void testSinks()
    {
        final Set<String> expectedSinks = new HashSet<>(Arrays.asList(PLATINUM, GOLD, COPPER));
        assertTrue(this.graph.getSinks().stream().allMatch(expectedSinks::remove)
                && expectedSinks.isEmpty());
    }

    @Test
    public void testSources()
    {
        final Set<String> expectedSources = new HashSet<>(Arrays.asList(METAL));
        assertTrue(this.graph.getSources().stream().allMatch(expectedSources::remove)
                && expectedSources.isEmpty());
    }

    @Test
    public void testTopologicalSort()
    {
        final Stack<String> topologicalSort = this.graph.getTopologicalSortedList();
        Assert.assertEquals(METAL, topologicalSort.pop());
        Assert.assertEquals(COINAGE, topologicalSort.pop());
        Assert.assertEquals(PRECIOUS, topologicalSort.pop());
        Assert.assertEquals(COPPER, topologicalSort.pop());
        Assert.assertEquals(GOLD, topologicalSort.pop());
        Assert.assertEquals(PLATINUM, topologicalSort.pop());
    }
}
