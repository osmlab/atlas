package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Unit test rule for {@link CountrySliceTest}.
 *
 * @author bbreithaupt
 */
public class CountrySliceTestRule extends CoreTestRule
{
    private static final String ONE = "62.614939, -141.000650";
    private static final String TWO = "62.615151, -141.001114";
    private static final String THREE = "62.615304, -141.001447";

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
                    @Node(id = "2000000", coordinates = @Loc(value = TWO)),
                    @Node(id = "3000000", coordinates = @Loc(value = THREE), tags = {
                            "synthetic_boundary_node=yes" }) },
            // Areas
            areas = {
                    @Area(id = "1000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = ONE) }),
                    @Area(id = "2001000", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = ONE) }) },
            // Edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE) }) },
            // Lines
            lines = { @Line(id = "1000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Line(id = "2001000", coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE) }) },
            // Points
            points = { @Point(id = "1000000", coordinates = @Loc(value = ONE)),
                    @Point(id = "2000000", coordinates = @Loc(value = THREE)) },
            // Relations
            relations = {
                    @Relation(id = "1000000", members = {
                            @Member(id = "1000000", role = "", type = "node"),
                            @Member(id = "1000000", role = "", type = "area") }),
                    @Relation(id = "2000000", members = {
                            @Member(id = "1000000", role = "", type = "node"),
                            @Member(id = "2001000", role = "", type = "area") }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
