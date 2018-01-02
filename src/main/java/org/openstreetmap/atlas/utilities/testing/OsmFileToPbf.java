package org.openstreetmap.atlas.utilities.testing;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;

import crosby.binary.osmosis.OsmosisSerializer;

/**
 * @author matthieun
 */
public class OsmFileToPbf
{
    public void update(final Resource osmFile, final WritableResource pbfFile)
    {
        final OsmosisXmlReaderFromResource osmReader = new OsmosisXmlReaderFromResource(osmFile,
                true, CompressionMethod.None);
        final OsmosisSerializer pbfWriter = new OsmosisSerializer(
                new BlockOutputStream(pbfFile.write()));
        osmReader.setSink(pbfWriter);
        osmReader.run();
    }
}
