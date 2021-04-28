package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class AtlasSearchCommandTest
{
    @Test
    public void testBoundingPolygonSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--bounding-polygon=POLYGON((0 0, 0 3, 3 3, 3 0, 0 0))");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "geometry: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompletePoint [\n" + "identifier: 2000000, \n" + "geometry: POINT (2 2), \n"
                    + "tags: {baz=bat}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((2 2, 2 2, 2 2, 2 2, 2 2)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testCollectSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);

            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--geometry=POINT (1 1)",
                    "--collect-matching", "--output=/Users/foo");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "geometry: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n"
                            + "find: saved to /Users/foo/collected-multi.atlas\n",
                    errContent.toString());
            Assert.assertTrue(new File("/Users/foo/collected-multi.atlas", filesystem).exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testFailSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--type=ASD");

            Assert.assertEquals("", outContent.toString());
            Assert.assertEquals(
                    "find: error: could not parse ItemType 'ASD'\n"
                            + "find: error: no filtering objects were successfully constructed\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testGeometrySearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);

            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--geometry=POINT (1 1)");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "geometry: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--geometry=LINESTRING (10 10, 11 11, 12 12)");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteLine [\n" + "identifier: 4000000, \n"
                    + "polyLine: LINESTRING (10 10, 11 11, 12 12), \n"
                    + "tags: {this_is=a_line}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((10 10, 10 12, 12 12, 12 10, 10 10)), \n" + "]\n" + "\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent2.toString());

            final ByteArrayOutputStream outContent3 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent3 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent3));
            command.setNewErrStream(new PrintStream(errContent3));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--geometry=POLYGON ((20 20, 21 20, 21 21, 20 20))");

            Assert.assertEquals(
                    "Found entity matching criteria in /Users/foo/test.atlas:\n"
                            + "CompleteArea [\n" + "identifier: 7000000, \n"
                            + "geometry: POLYGON ((20 20, 21 20, 21 21, 20 20)), \n"
                            + "tags: {baz=bat}, \n" + "parentRelations: [12000000], \n"
                            + "bounds: POLYGON ((20 20, 20 21, 21 21, 21 20, 20 20)), \n" + "]\n\n",
                    outContent3.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent3.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testIdSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--id=1000000");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "geometry: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testInOutEdgeSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--out-edge=11000001");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteNode [\n" + "identifier: 9000000, \n" + "geometry: POINT (32 32), \n"
                    + "tags: {baz=bat}, \n" + "inEdges: [11000000], \n" + "outEdges: [11000001], \n"
                    + "parentRelations: [], \n"
                    + "bounds: POLYGON ((32 32, 32 32, 32 32, 32 32, 32 32)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--in-edge=11000000");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteNode [\n" + "identifier: 9000000, \n" + "geometry: POINT (32 32), \n"
                    + "tags: {baz=bat}, \n" + "inEdges: [11000000], \n" + "outEdges: [11000001], \n"
                    + "parentRelations: [], \n"
                    + "bounds: POLYGON ((32 32, 32 32, 32 32, 32 32, 32 32)), \n" + "]\n\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent2.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testJsonOption()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--all", "--json");

            final StringResource expected = new StringResource();
            expected.writeAndClose(AtlasSearchCommandTest.class
                    .getResourceAsStream("expected-json-features.txt").readAllBytes());
            Assert.assertEquals(expected.all(), outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testOsmIdSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--osmid=11");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteEdge [\n" + "identifier: 11000000, \n"
                    + "geometry: LINESTRING (30 30, 31 30, 32 32), \n" + "startNode: 8000000, \n"
                    + "endNode: 9000000, \n" + "tags: {foo=bar}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteEdge [\n" + "identifier: 11000001, \n"
                    + "geometry: LINESTRING (32 32, 32 33, 34 34), \n" + "startNode: 9000000, \n"
                    + "endNode: 10000000, \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((32 32, 32 34, 34 34, 34 32, 32 32)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testParentRelationSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--parent-relations=12000000");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteEdge [\n" + "identifier: 11000000, \n"
                    + "geometry: LINESTRING (30 30, 31 30, 32 32), \n" + "startNode: 8000000, \n"
                    + "endNode: 9000000, \n" + "tags: {foo=bar}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteEdge [\n" + "identifier: 11000001, \n"
                    + "geometry: LINESTRING (32 32, 32 33, 34 34), \n" + "startNode: 9000000, \n"
                    + "endNode: 10000000, \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((32 32, 32 34, 34 34, 34 32, 32 32)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteArea [\n" + "identifier: 7000000, \n"
                    + "geometry: POLYGON ((20 20, 21 20, 21 21, 20 20)), \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((20 20, 20 21, 21 21, 21 20, 20 20)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testPredicateSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--predicate=!e.relations().isEmpty() && e.relations().collect { it.getIdentifier() }.containsAll(Sets.hashSet(12000000L))",
                    "--imports=org.openstreetmap.atlas.utilities.command.parsing");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteEdge [\n" + "identifier: 11000000, \n"
                    + "geometry: LINESTRING (30 30, 31 30, 32 32), \n" + "startNode: 8000000, \n"
                    + "endNode: 9000000, \n" + "tags: {foo=bar}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteEdge [\n" + "identifier: 11000001, \n"
                    + "geometry: LINESTRING (32 32, 32 33, 34 34), \n" + "startNode: 9000000, \n"
                    + "endNode: 10000000, \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((32 32, 32 34, 34 34, 34 32, 32 32)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteArea [\n" + "identifier: 7000000, \n"
                    + "geometry: POLYGON ((20 20, 21 20, 21 21, 20 20)), \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((20 20, 20 21, 21 21, 21 20, 20 20)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testRelationMemberAND()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--and-relation-members=EDGE,11000000,role0;EDGE,11000001,role1");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteRelation [\n" + "identifier: 12000000, \n" + "tags: {route=bike}, \n"
                    + "members: RelationBean [[[EDGE, 11000000, role0], [EDGE, 11000001, role1], [AREA, 7000000, role2]]], \n"
                    + "parentRelations: [], \n"
                    + "bounds: POLYGON ((20 20, 20 34, 34 34, 34 20, 20 20)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());

        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testRelationMemberANDParseError()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--and-relation-members=EDGE,asd,*");

            Assert.assertEquals("", outContent2.toString());
            Assert.assertEquals(
                    "find: error: could not parse ID `asd' from member `EDGE,asd,*'\n"
                            + "find: error: no filtering objects were successfully constructed\n",
                    errContent2.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testRelationMemberOR()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--relation-members=AREA,*,*;*,11000001,*");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteRelation [\n" + "identifier: 12000000, \n" + "tags: {route=bike}, \n"
                    + "members: RelationBean [[[EDGE, 11000000, role0], [EDGE, 11000001, role1], [AREA, 7000000, role2]]], \n"
                    + "parentRelations: [], \n"
                    + "bounds: POLYGON ((20 20, 20 34, 34 34, 34 20, 20 20)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--relation-members=POINT,*,\\*");

            Assert.assertEquals(
                    "Found entity matching criteria in /Users/foo/test.atlas:\n"
                            + "CompleteRelation [\n" + "identifier: 1300000, \n"
                            + "tags: {some=relation}, \n"
                            + "members: RelationBean [[[POINT, 4000000, *]]], \n"
                            + "parentRelations: [], \n"
                            + "bounds: POLYGON ((4 5, 4 5, 4 5, 4 5, 4 5)), \n" + "]\n" + "\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent2.toString());

            final ByteArrayOutputStream outContent3 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent3 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent3));
            command.setNewErrStream(new PrintStream(errContent3));

            // Find relations with an emoji role
            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--relation-members=*,*,💯");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteRelation [\n" + "identifier: 1400000, \n"
                    + "tags: {some2=relation2}, \n"
                    + "members: RelationBean [[[POINT, 5000000, 💯]]], \n"
                    + "parentRelations: [], \n"
                    + "bounds: POLYGON ((30 30, 30 30, 30 30, 30 30, 30 30)), \n" + "]\n" + "\n",
                    outContent3.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent3.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testRelationMemberORParseError()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            AtlasSearchCommand command = new AtlasSearchCommand();
            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--relation-members=asd,*,*");

            Assert.assertEquals("", outContent2.toString());
            Assert.assertEquals(
                    "find: error: could not parse ItemType `asd' from member `asd,*,*'\n"
                            + "find: error: no filtering objects were successfully constructed\n",
                    errContent2.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testShowAll()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test2.atlas", "--verbose", "--all");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test2.atlas:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "geometry: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test2.atlas\n"
                            + "find: processing atlas /Users/foo/test2.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStartEndNodeSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--start-node=8000000",
                    "--end-node=9000000");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteEdge [\n" + "identifier: 11000000, \n"
                    + "geometry: LINESTRING (30 30, 31 30, 32 32), \n" + "startNode: 8000000, \n"
                    + "endNode: 9000000, \n" + "tags: {foo=bar}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testSubGeometrySearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);

            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--sub-geometry=POINT (1 1)");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "geometry: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--sub-geometry=POINT (10 10)");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteLine [\n" + "identifier: 4000000, \n"
                    + "polyLine: LINESTRING (10 10, 11 11, 12 12), \n"
                    + "tags: {this_is=a_line}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((10 10, 10 12, 12 12, 12 10, 10 10)), \n" + "]\n" + "\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent2.toString());

            final ByteArrayOutputStream outContent3 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent3 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent3));
            command.setNewErrStream(new PrintStream(errContent3));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--sub-geometry=LINESTRING (10 10, 11 11)");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteLine [\n" + "identifier: 4000000, \n"
                    + "polyLine: LINESTRING (10 10, 11 11, 12 12), \n"
                    + "tags: {this_is=a_line}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((10 10, 10 12, 12 12, 12 10, 10 10)), \n" + "]\n" + "\n",
                    outContent3.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent3.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testTagSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--tag-filter=another->*");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteNode [\n" + "identifier: 10000000, \n"
                    + "geometry: POINT (34 34), \n" + "tags: {another=tag2}, \n"
                    + "inEdges: [11000001], \n" + "outEdges: [], \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((34 34, 34 34, 34 34, 34 34, 34 34)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteLine [\n" + "identifier: 6000000, \n"
                    + "polyLine: LINESTRING (16 16, 17 17, 18 18), \n"
                    + "tags: {another=tag1, hello=world}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((16 16, 16 18, 18 18, 18 16, 16 16)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose",
                    "--tag-matcher=another=/tag.*/");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteNode [\n" + "identifier: 10000000, \n"
                    + "geometry: POINT (34 34), \n" + "tags: {another=tag2}, \n"
                    + "inEdges: [11000001], \n" + "outEdges: [], \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((34 34, 34 34, 34 34, 34 34, 34 34)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteLine [\n" + "identifier: 6000000, \n"
                    + "polyLine: LINESTRING (16 16, 17 17, 18 18), \n"
                    + "tags: {another=tag1, hello=world}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((16 16, 16 18, 18 18, 18 16, 16 16)), \n" + "]\n" + "\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent2.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testTypeSearch()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas", "--verbose", "--type=AREA");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas:\n"
                    + "CompleteArea [\n" + "identifier: 7000000, \n"
                    + "geometry: POLYGON ((20 20, 21 20, 21 21, 20 20)), \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((20 20, 20 21, 21 21, 21 20, 20 20)), \n" + "]\n" + "\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas\n"
                            + "find: processing atlas /Users/foo/test.atlas (1/1)\n",
                    errContent.toString());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem)
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();

        builder.addPoint(1000000L, Location.forWkt("POINT(1 1)"), Maps.hashMap("foo", "bar"));
        builder.addPoint(2000000L, Location.forWkt("POINT(2 2)"), Maps.hashMap("baz", "bat"));
        builder.addPoint(3000000L, Location.forWkt("POINT(3 3)"),
                Maps.hashMap("foo", "bar", "baz", "bat"));
        builder.addPoint(4000000L, Location.forWkt("POINT(4 5)"), Maps.hashMap("a", "b"));
        builder.addPoint(5000000L, Location.forWkt("POINT(30 30)"), Maps.hashMap("a", "b"));

        builder.addLine(4000000L, PolyLine.wkt("LINESTRING(10 10, 11 11, 12 12)"),
                Maps.hashMap("this_is", "a_line"));
        builder.addLine(5000000L, PolyLine.wkt("LINESTRING(13 13, 14 14, 15 15)"),
                Maps.hashMap("this_is", "a_line", "foo", "bar"));
        builder.addLine(6000000L, PolyLine.wkt("LINESTRING(16 16, 17 17, 18 18)"),
                Maps.hashMap("hello", "world", "another", "tag1"));

        builder.addArea(7000000L, Polygon.wkt("POLYGON((20 20, 21 20, 21 21, 20 20))"),
                Maps.hashMap("baz", "bat"));

        builder.addNode(8000000L, Location.forWkt("POINT(30 30)"), Maps.hashMap("foo", "bar"));
        builder.addNode(9000000L, Location.forWkt("POINT(32 32)"), Maps.hashMap("baz", "bat"));
        builder.addNode(10000000L, Location.forWkt("POINT(34 34)"),
                Maps.hashMap("another", "tag2"));

        builder.addEdge(11000000L, PolyLine.wkt("LINESTRING(30 30, 31 30, 32 32)"),
                Maps.hashMap("foo", "bar"));
        builder.addEdge(11000001L, PolyLine.wkt("LINESTRING(32 32, 32 33, 34 34)"),
                Maps.hashMap("baz", "bat"));

        final RelationBean bean = new RelationBean();
        bean.addItem(11000000L, "role0", ItemType.EDGE);
        bean.addItem(11000001L, "role1", ItemType.EDGE);
        bean.addItem(7000000L, "role2", ItemType.AREA);
        builder.addRelation(12000000L, 12L, bean, Maps.hashMap("route", "bike"));

        final RelationBean bean2 = new RelationBean();
        bean2.addItem(4000000L, "*", ItemType.POINT);
        builder.addRelation(1300000L, 13L, bean2, Maps.hashMap("some", "relation"));

        final RelationBean bean3 = new RelationBean();
        bean3.addItem(5000000L, "💯", ItemType.POINT);
        builder.addRelation(1400000L, 14L, bean3, Maps.hashMap("some2", "relation2"));

        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas", filesystem);
        assert atlas != null;
        atlas.save(atlasFile);

        final PackedAtlasBuilder builder2 = new PackedAtlasBuilder();
        builder2.addPoint(1000000L, Location.forWkt("POINT(1 1)"), Maps.hashMap("foo", "bar"));
        final Atlas atlas2 = builder2.get();
        final File atlasFile2 = new File("/Users/foo/test2.atlas", filesystem);
        assert atlas2 != null;
        atlas2.save(atlasFile2);
    }
}
