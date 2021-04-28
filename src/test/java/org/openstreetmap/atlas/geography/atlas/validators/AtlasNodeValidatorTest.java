package org.openstreetmap.atlas.geography.atlas.validators;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.EmptyAtlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class AtlasNodeValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNodeToEdgeConnectivity()
    {
        final Atlas atlas = new EmptyAtlas()
        {
            private static final long serialVersionUID = 5914210513631166207L;

            @Override
            public Iterable<Node> nodes()
            {
                return Iterables.from(new CompleteNode(123L, null, null, null, null, null)
                {
                    private static final long serialVersionUID = 8589044244340528526L;

                    @Override
                    public SortedSet<Edge> inEdges()
                    {
                        // Here supply the comparator so TreeSet accepts a null.
                        final SortedSet<Edge> result = new TreeSet<>((edge1, edge2) -> 1);
                        result.add(null);
                        return result;
                    }
                });
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException
                .expectMessage("is logically disconnected from some referenced in edge.");

        new AtlasNodeValidator(atlas).validateNodeToEdgeConnectivity();
    }
}
