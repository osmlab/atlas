package org.openstreetmap.atlas.geography.atlas.geojson;

import static org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader.IS_ATLAS;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;

/**
 * Basic utils we use in our tippecanoe module
 *
 * @author hallahan
 */
public final class TippecanoeUtils
{
    private TippecanoeUtils()
    {
        // Util class
    }

    public static List<File> fetchAtlasFilesInDirectory(final Path directory)
    {
        return new File(directory.toFile()).listFilesRecursively().stream().filter(IS_ATLAS)
                .collect(Collectors.toList());
    }

    public static List<File> fetchGeoJsonFilesInDirectory(final Path directory)
    {
        return new File(directory.toFile()).listFilesRecursively().stream()
                .filter(FileSuffix.resourceFilter(FileSuffix.GEO_JSON))
                .collect(Collectors.toList());
    }
}
