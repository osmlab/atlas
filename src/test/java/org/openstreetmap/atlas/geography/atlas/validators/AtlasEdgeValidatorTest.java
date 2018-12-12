package org.openstreetmap.atlas.geography.atlas.validators;

import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class AtlasEdgeValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEdgeToNodeConnectivity()
    {
        final Atlas atlas = new BloatedAtlas()
        {
            private static final long serialVersionUID = 2810137078529598434L;

            @Override
            public Iterable<Edge> edges()
            {
                return Iterables.from(new BloatedEdge(123L, null, null, null, null, null)
                {
                    private static final long serialVersionUID = 8238381291474515199L;

                    @Override
                    public Node start()
                    {
                        return null;
                    }
                });
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is logically disconnected at its start.");

        new AtlasEdgeValidator(atlas).validateEdgeToNodeConnectivity();
    }

    @Test
    public void testEdgeToNodeLocationAccuracy()
    {
        final Atlas atlas = new BloatedAtlas()
        {
            private static final long serialVersionUID = -3111613839268225792L;

            @Override
            public Iterable<Edge> edges()
            {
                return Iterables.from(new BloatedEdge(123L,
                        new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER), null, null, null,
                        null)
                {
                    private static final long serialVersionUID = -1653164181164046228L;

                    @Override
                    public Node start()
                    {
                        return new BloatedNode(456L, Location.EIFFEL_TOWER, null, null, null, null);
                    }
                });
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match with its start Node");

        new AtlasEdgeValidator(atlas).validateEdgeToNodeLocationAccuracy();

        final Atlas atlas2 = new BloatedAtlas()
        {
            private static final long serialVersionUID = -3557437996775535655L;

            @Override
            public Iterable<Edge> edges()
            {
                return Iterables.from(new BloatedEdge(123L,
                        new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER), null, null, null,
                        null)
                {
                    private static final long serialVersionUID = 2460452389716155618L;

                    @Override
                    public Node end()
                    {
                        return new BloatedNode(456L, Location.COLOSSEUM, null, null, null, null);
                    }
                });
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match with its end Node");

        new AtlasEdgeValidator(atlas2).validateEdgeToNodeLocationAccuracy();
    }

    @Test
    public void testReverseEdgePolyLineUpdated()
    {
        final Atlas atlas = new BloatedAtlas()
        {
            private static final long serialVersionUID = -1125897101453459977L;

            @Override
            public Edge edge(final long identifier)
            {
                return new BloatedEdge(-123L, null, null, null, null, null);
            }

            @Override
            public Iterable<Edge> edges(final Predicate<Edge> matcher)
            {
                return Iterables.from(new BloatedEdge(123L,
                        new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER), null, null, null,
                        null)
                {
                    private static final long serialVersionUID = -9016561382807198568L;

                    @Override
                    public Optional<Edge> reversed()
                    {
                        return Optional.of(new BloatedEdge(-123L,
                                new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER), null, null,
                                null, null));
                    }
                });
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("have mismatching PolyLines: Forward =");

        new AtlasEdgeValidator(atlas).validateReverseEdgePolyLineUpdated();
    }
}
