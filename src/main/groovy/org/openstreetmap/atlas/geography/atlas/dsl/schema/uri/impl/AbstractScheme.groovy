package org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl

import org.openstreetmap.atlas.geography.MultiPolygon
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.Scheme
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.AtlasSectionProcessor
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource
import org.openstreetmap.atlas.streaming.resource.InputStreamResource
import org.openstreetmap.atlas.utilities.testing.OsmFileToPbf

/**
 * An abstraction of a scheme.
 *
 * @author Yazad Khambata
 */
abstract class AbstractScheme implements Scheme {
    protected Atlas osmInputStreamResourceToAtlas(final InputStreamResource inputStreamResource) {
        Valid.notEmpty inputStreamResource

        final ByteArrayResource pbfFile = new ByteArrayResource()
        new OsmFileToPbf().update(inputStreamResource, pbfFile)

        final AtlasLoadingOption loadingOption = AtlasLoadingOption.withNoFilter()
        final Atlas rawAtlas = new RawAtlasGenerator(pbfFile, loadingOption,
                MultiPolygon.MAXIMUM).build()

        // Way-section
        final Atlas atlas = new AtlasSectionProcessor(rawAtlas, loadingOption).run()

        atlas
    }

    protected Atlas merge(List<Atlas> atlases) {
        final Atlas atlas = new MultiAtlas(atlases)
        atlas
    }
}
