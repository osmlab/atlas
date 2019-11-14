package org.openstreetmap.atlas.geography.atlas.dsl.console

import org.junit.Assert
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.StandardOutputConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.QuietConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class ConsoleWriterTest extends AbstractAQLTest {
    @Test
    void testQuietConsoleWriter() {
        final ConsoleWriter consoleWriter = QuietConsoleWriter.getInstance()
        Assert.assertNotNull(consoleWriter)
        Assert.assertTrue(consoleWriter.isTurnedOff())
        consoleWriter.echo("Hello")
    }

    @Test
    void testBasicConsoleWriter() {
        final ConsoleWriter consoleWriter = StandardOutputConsoleWriter.getInstance()
        Assert.assertNotNull(consoleWriter)
        Assert.assertFalse(consoleWriter.isTurnedOff())
        consoleWriter.echo("Hello")
    }

    @Test
    void testNullString() {
        final ConsoleWriter consoleWriter = StandardOutputConsoleWriter.getInstance()
        consoleWriter.echo((String)null)
    }

    @Test
    void testNullObject() {
        final ConsoleWriter consoleWriter = StandardOutputConsoleWriter.getInstance()
        consoleWriter.echo(null)
    }


    @Test
    void testExplain() {
        def atlas = usingAlcatraz()
        def select1 = select relation.id from atlas.relation where relation.hasId(1)
        ExplainerImpl.instance.explain select1
    }
}
