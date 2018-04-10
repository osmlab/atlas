package org.openstreetmap.atlas.streaming.resource;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Joiner;

/**
 * @author mgostintsev
 */
public enum FileSuffix
{
    ATLAS(".atlas"),
    CHANGESET(".cs"),
    CSV(".csv"),
    GEO_JSON(".geojson"),
    GZIP(".gz"),
    // extended csv
    EXTENDED(".ext"),
    JSON(".json"),
    PBF(".pbf"),
    PROTOATLAS(".patlas"),
    TEMPORARY(".tmp"),
    TEXT(".txt"),
    ZIP(".zip"),
    WKT(".wkt"),
    WKB(".wkb");

    private String suffix;

    public static FileSuffix getEnum(final String value)
    {
        return suffixFor(value).orElseThrow(
                () -> new IllegalArgumentException("No file suffix found for " + value));
    }

    public static Predicate<Path> pathFilter(final FileSuffix... listOfSuffixes)
    {
        final String suffix = Joiner.on("").join(listOfSuffixes);
        return path ->
        {
            return path.getFileName().toString().toLowerCase().endsWith(suffix);
        };
    }

    public static Predicate<Resource> resourceFilter(final FileSuffix... listOfSuffixes)
    {
        final String suffix = Joiner.on("").join(listOfSuffixes);
        return resource ->
        {
            return resource.getName() == null || resource.getName().endsWith(suffix);
        };
    }

    public static Optional<FileSuffix> suffixFor(final String value)
    {
        final String compareMe = value.toLowerCase();
        return Stream.of(FileSuffix.values())
                .filter(suffix -> compareMe.endsWith(suffix.toString())).findFirst();
    }

    FileSuffix(final String suffix)
    {
        this.suffix = suffix;
    }

    public boolean matches(final Resource resource)
    {
        final Optional<FileSuffix> foundSuffix = suffixFor(resource.getName());
        return foundSuffix.isPresent() && foundSuffix.get() == this;
    }

    @Override
    public String toString()
    {
        return this.suffix;
    }
}
