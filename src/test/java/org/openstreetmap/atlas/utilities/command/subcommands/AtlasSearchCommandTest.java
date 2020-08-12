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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--bounding-polygon=POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "location: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompletePoint [\n" + "identifier: 2000000, \n" + "location: POINT (2 2), \n"
                    + "tags: {baz=bat}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((2 2, 2 2, 2 2, 2 2, 2 2)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompletePoint [\n" + "identifier: 3000000, \n" + "location: POINT (3 3), \n"
                    + "tags: {baz=bat, foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((3 3, 3 3, 3 3, 3 3, 3 3)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--geometry=POINT (1 1)", "--collect-matching", "--output=/Users/foo");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "location: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n"
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose", "--type=ASD");

            Assert.assertEquals("", outContent.toString());
            Assert.assertEquals(
                    "find: warn: could not parse ItemType 'ASD': skipping...\n"
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--geometry=POINT (1 1)");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "location: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--geometry=LINESTRING (10 10, 11 11, 12 12)");

            Assert.assertEquals(
                    "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                            + "CompleteLine [\n" + "identifier: 4000000, \n"
                            + "polyLine: LINESTRING (10 10, 11 11, 12 12), \n"
                            + "tags: {this_is=a_line}, \n" + "parentRelations: [], \n"
                            + "bounds: POLYGON ((10 10, 10 12, 12 12, 12 10, 10 10)), \n" + "]\n\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
                    errContent2.toString());

            final ByteArrayOutputStream outContent3 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent3 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent3));
            command.setNewErrStream(new PrintStream(errContent3));

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--geometry=POLYGON ((20 20, 21 20, 21 21, 20 20))");

            Assert.assertEquals(
                    "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                            + "CompleteArea [\n" + "identifier: 7000000, \n"
                            + "polygon: POLYGON ((20 20, 21 20, 21 21, 20 20)), \n"
                            + "tags: {baz=bat}, \n" + "parentRelations: [], \n"
                            + "bounds: POLYGON ((20 20, 20 21, 21 21, 21 20, 20 20)), \n" + "]\n\n",
                    outContent3.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose", "--id=1000000");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "location: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose", "--out-edge=11000001");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteNode [\n" + "identifier: 9000000, \n" + "location: POINT (32 32), \n"
                    + "inEdges: [11000000], \n" + "outEdges: [11000001], \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [], \n"
                    + "bounds: POLYGON ((32 32, 32 32, 32 32, 32 32, 32 32)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose", "--in-edge=11000000");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteNode [\n" + "identifier: 9000000, \n" + "location: POINT (32 32), \n"
                    + "inEdges: [11000000], \n" + "outEdges: [11000001], \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [], \n"
                    + "bounds: POLYGON ((32 32, 32 32, 32 32, 32 32, 32 32)), \n" + "]\n\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
                    errContent2.toString());
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose", "--osmid=11");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteEdge [\n" + "identifier: 11000000, \n"
                    + "polyLine: LINESTRING (30 30, 31 30, 32 32), \n" + "startNode: 8000000, \n"
                    + "endNode: 9000000, \n" + "tags: {foo=bar}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteEdge [\n" + "identifier: 11000001, \n"
                    + "polyLine: LINESTRING (32 32, 32 33, 34 34), \n" + "startNode: 9000000, \n"
                    + "endNode: 10000000, \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((32 32, 32 34, 34 34, 34 32, 32 32)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--parent-relations=12000000");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteEdge [\n" + "identifier: 11000000, \n"
                    + "polyLine: LINESTRING (30 30, 31 30, 32 32), \n" + "startNode: 8000000, \n"
                    + "endNode: 9000000, \n" + "tags: {foo=bar}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteEdge [\n" + "identifier: 11000001, \n"
                    + "polyLine: LINESTRING (32 32, 32 33, 34 34), \n" + "startNode: 9000000, \n"
                    + "endNode: 10000000, \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((32 32, 32 34, 34 34, 34 32, 32 32)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--predicate=!e.relations().isEmpty() && e.relations().collect { it.getIdentifier() }.containsAll(Sets.hashSet(12000000L))",
                    "--imports=org.openstreetmap.atlas.utilities.command.parsing");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteEdge [\n" + "identifier: 11000000, \n"
                    + "polyLine: LINESTRING (30 30, 31 30, 32 32), \n" + "startNode: 8000000, \n"
                    + "endNode: 9000000, \n" + "tags: {foo=bar}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteEdge [\n" + "identifier: 11000001, \n"
                    + "polyLine: LINESTRING (32 32, 32 33, 34 34), \n" + "startNode: 9000000, \n"
                    + "endNode: 10000000, \n" + "tags: {baz=bat}, \n"
                    + "parentRelations: [12000000], \n"
                    + "bounds: POLYGON ((32 32, 32 34, 34 34, 34 32, 32 32)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose", "--start-node=8000000",
                    "--end-node=9000000");

            Assert.assertEquals(
                    "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                            + "CompleteEdge [\n" + "identifier: 11000000, \n"
                            + "polyLine: LINESTRING (30 30, 31 30, 32 32), \n"
                            + "startNode: 8000000, \n" + "endNode: 9000000, \n"
                            + "tags: {foo=bar}, \n" + "parentRelations: [12000000], \n"
                            + "bounds: POLYGON ((30 30, 30 32, 32 32, 32 30, 30 30)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--sub-geometry=POINT (1 1)");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompletePoint [\n" + "identifier: 1000000, \n" + "location: POINT (1 1), \n"
                    + "tags: {foo=bar}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((1 1, 1 1, 1 1, 1 1, 1 1)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
                    errContent.toString());

            final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent2));
            command.setNewErrStream(new PrintStream(errContent2));

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--sub-geometry=POINT (10 10)");

            Assert.assertEquals(
                    "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                            + "CompleteLine [\n" + "identifier: 4000000, \n"
                            + "polyLine: LINESTRING (10 10, 11 11, 12 12), \n"
                            + "tags: {this_is=a_line}, \n" + "parentRelations: [], \n"
                            + "bounds: POLYGON ((10 10, 10 12, 12 12, 12 10, 10 10)), \n" + "]\n\n",
                    outContent2.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
                    errContent2.toString());

            final ByteArrayOutputStream outContent3 = new ByteArrayOutputStream();
            final ByteArrayOutputStream errContent3 = new ByteArrayOutputStream();
            command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent3));
            command.setNewErrStream(new PrintStream(errContent3));

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--sub-geometry=LINESTRING (10 10, 11 11)");

            Assert.assertEquals(
                    "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                            + "CompleteLine [\n" + "identifier: 4000000, \n"
                            + "polyLine: LINESTRING (10 10, 11 11, 12 12), \n"
                            + "tags: {this_is=a_line}, \n" + "parentRelations: [], \n"
                            + "bounds: POLYGON ((10 10, 10 12, 12 12, 12 10, 10 10)), \n" + "]\n\n",
                    outContent3.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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
            final AtlasSearchCommand command = new AtlasSearchCommand();
            command.setNewFileSystem(filesystem);
            command.setNewOutStream(new PrintStream(outContent));
            command.setNewErrStream(new PrintStream(errContent));

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose",
                    "--taggable-filter=another->*");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteNode [\n" + "identifier: 10000000, \n"
                    + "location: POINT (34 34), \n" + "inEdges: [11000001], \n" + "outEdges: [], \n"
                    + "tags: {another=tag2}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((34 34, 34 34, 34 34, 34 34, 34 34)), \n" + "]\n" + "\n"
                    + "Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteLine [\n" + "identifier: 6000000, \n"
                    + "polyLine: LINESTRING (16 16, 17 17, 18 18), \n"
                    + "tags: {another=tag1, hello=world}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((16 16, 16 18, 18 18, 18 16, 16 16)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
                    errContent.toString());
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

            command.runSubcommand("/Users/foo/test.atlas.txt", "--verbose", "--type=RELATION");

            Assert.assertEquals("Found entity matching criteria in /Users/foo/test.atlas.txt:\n"
                    + "CompleteRelation [\n" + "identifier: 12000000, \n"
                    + "bounds: POLYGON ((30 30, 30 34, 34 34, 34 30, 30 30)), \n"
                    + "members: RelationBean [[[EDGE, 11000000, role0], [EDGE, 11000001, role1]]], \n"
                    + "tags: {route=bike}, \n" + "parentRelations: [], \n"
                    + "bounds: POLYGON ((30 30, 30 34, 34 34, 34 30, 30 30)), \n" + "]\n\n",
                    outContent.toString());
            Assert.assertEquals(
                    "find: loading /Users/foo/test.atlas.txt\n"
                            + "find: processing atlas /Users/foo/test.atlas.txt (1/1)\n",
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
        builder.addRelation(12000000L, 12L, bean, Maps.hashMap("route", "bike"));

        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas.txt", filesystem);
        assert atlas != null;
        atlas.saveAsText(atlasFile);
    }
}
