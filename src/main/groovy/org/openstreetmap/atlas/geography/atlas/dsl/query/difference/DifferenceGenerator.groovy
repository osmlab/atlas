package org.openstreetmap.atlas.geography.atlas.dsl.query.difference

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.change.diff.AtlasDiff
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.StandardOutputConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema

/**
 * Generates the difference between 2 atlas schemas.
 *
 * @author Yazad Khambata
 */
@Singleton
class DifferenceGenerator {

    Difference generateAndDumpDifference(final AtlasSchema atlasSchema1, final AtlasSchema atlasSchema2, final ConsoleWriter consoleWriter) {
        final Atlas atlas1 = atlasSchema1.atlasMediator.atlas
        final Atlas atlas2 = atlasSchema2.atlasMediator.atlas

        final AtlasDiff atlasDiff = new AtlasDiff(atlas1, atlas2)

        final Optional<Change> optionalChange = atlasDiff.generateChange()

        final Difference difference = Difference.builder()
                .atlasSchema1(atlasSchema1)
                .atlasSchema2(atlasSchema2)
                .change(optionalChange.orElseGet { -> null })
                .build()

        consoleWriter.echo(difference)

        difference
    }

    Difference generateAndDumpDifference(final AtlasSchema atlasSchema1, final AtlasSchema atlasSchema2) {
        this.generateAndDumpDifference(atlasSchema1, atlasSchema2, StandardOutputConsoleWriter.getInstance())
    }
}
