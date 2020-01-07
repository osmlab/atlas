package org.openstreetmap.atlas.geography.atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load an {@link Atlas} from a {@link Resource} or an {@link Iterable} of {@link Resource}s.
 * Supports also loading based on a resource name filter. Note that by default, this class will
 * filter the provided {@link Iterable} and remove any {@link Resource} that does not have a valid
 * atlas file extension (defined in the {@link FileSuffix} enum). This functionality can be disabled
 * by calling {@link AtlasResourceLoader#withAtlasFileExtensionFilterSetTo(boolean)} with
 * {@code false}. Disabling this functionality is useful if combining this class with atlases
 * fetched from a cache that does not respect the .atlas file extension convention.
 *
 * @author cstaylor
 * @author mgostintsev
 * @author matthieun
 * @author remegraw
 */
public class AtlasResourceLoader
{
    /**
     * @author matthieun
     */
    protected static final class AtlasFileSelector
    {
        public List<Resource> select(final File file)
        {
            final List<Resource> result = new ArrayList<>();
            if (file != null && file.exists())
            {
                file.listFilesRecursively().forEach(child ->
                {
                    if (child.isGzipped())
                    {
                        child.setDecompressor(Decompressor.GZIP);
                    }
                    result.add(child);
                });
            }
            else
            {
                logger.warn("File {} does not exist.", file);
            }
            return result;
        }
    }

    public static final Predicate<Resource> IS_ATLAS = FileSuffix.resourceFilter(FileSuffix.ATLAS)
            .or(FileSuffix.resourceFilter(FileSuffix.ATLAS, FileSuffix.GZIP));
    public static final Predicate<Resource> IS_TEXT_ATLAS = FileSuffix
            .resourceFilter(FileSuffix.ATLAS, FileSuffix.TEXT);

    private static final Logger logger = LoggerFactory.getLogger(AtlasResourceLoader.class);

    private final Predicate<Resource> alwaysTrueAtlasFilter = resource -> true;

    private Predicate<Resource> resourceFilter;
    private Predicate<AtlasEntity> atlasEntityFilter;
    private String multiAtlasName;
    private boolean filterForAtlasFileExtension = true;

    public AtlasResourceLoader()
    {
        this.resourceFilter = resource -> true;
        this.atlasEntityFilter = null;
    }

    public Atlas load(final Iterable<? extends Resource> input)
    {
        final Predicate<Resource> toggleableAtlasFileFilter = this.filterForAtlasFileExtension
                ? IS_ATLAS
                : this.alwaysTrueAtlasFilter;

        /*
         * FIXME These filters are wonky when the resource name is null, since the name filters let
         * null-named resources pass through. What we should do is disallow resources will
         * null-names, and fix all unit tests that forget to set a resource name.
         */
        final List<Resource> binaryResources = Iterables.stream(input).flatMap(this::resourcesIn)
                .filter(toggleableAtlasFileFilter).filter(this.resourceFilter).collectToList();
        final List<Resource> textResources = Iterables.stream(input).flatMap(this::resourcesIn)
                .filter(IS_TEXT_ATLAS).filter(this.resourceFilter).collectToList();

        final long size = binaryResources.size() + (long) textResources.size();
        if (size == 1)
        {
            return loadSingleAtlas(binaryResources, textResources);
        }
        else if (size > 1)
        {
            return loadMultipleAtlases(binaryResources, textResources);
        }
        else
        {
            return null;
        }
    }

    public Atlas load(final Resource... resource)
    {
        return load(Iterables.from(resource));
    }

    /**
     * Optionally add an {@link AtlasEntity} filter
     *
     * @param filter
     *            filter which {@link AtlasEntity}s to include/exclude in the {@link Atlas}
     */
    public void setAtlasEntityFilter(final Predicate<AtlasEntity> filter)
    {
        this.atlasEntityFilter = filter;
    }

    /**
     * Optionally add a {@link Resource} filter
     *
     * @param filter
     *            filter which {@link Resource} to load
     */
    public void setResourceFilter(final Predicate<Resource> filter)
    {
        this.resourceFilter = filter;
    }

    /**
     * Optionally add an {@link AtlasEntity} filter
     *
     * @param filter
     *            filter which {@link AtlasEntity}s to include/exclude in the {@link Atlas}
     * @return fluent interface requires this be returned
     */
    public AtlasResourceLoader withAtlasEntityFilter(final Predicate<AtlasEntity> filter)
    {
        setAtlasEntityFilter(filter);
        return this;
    }

    /**
     * Enable or disable atlas file extension filtering on this loader.
     *
     * @param value
     *            whether to enable or disable
     * @return the modified {@link AtlasResourceLoader}
     */
    public AtlasResourceLoader withAtlasFileExtensionFilterSetTo(final boolean value)
    {
        this.filterForAtlasFileExtension = value;
        return this;
    }

    public AtlasResourceLoader withMultiAtlasName(final String multiAtlasName)
    {
        this.multiAtlasName = multiAtlasName;
        return this;
    }

    /**
     * Optionally add a {@link Resource} filter
     *
     * @param filter
     *            filter which {@link Resource} to load
     * @return fluent interface requires this be returned
     */
    public AtlasResourceLoader withResourceFilter(final Predicate<Resource> filter)
    {
        setResourceFilter(filter);
        return this;
    }

    private Atlas loadMultipleAtlases(final List<Resource> binaryResources,
            final List<Resource> textResources)
    {
        /*
         * There are three scenarios that must be handled. 1) There were only binary atlases. 2)
         * There was a mix of binary and text atlases. 3) There were only text atlases.
         */

        MultiAtlas resultAtlas = null;
        if (!binaryResources.isEmpty())
        {
            resultAtlas = this.atlasEntityFilter == null
                    ? MultiAtlas.loadFromPackedAtlas(binaryResources)
                    : MultiAtlas.loadFromPackedAtlas(binaryResources, this.atlasEntityFilter);
        }
        if (!textResources.isEmpty())
        {
            final List<Atlas> textAtlases = loadTextAtlasesFromResources(textResources);
            if (!textAtlases.isEmpty())
            {
                final MultiAtlas textMultiAtlas = new MultiAtlas(textAtlases);
                /*
                 * In this case, 'resultAtlas' is not null because there was a mix of binary and
                 * text atlases.
                 */
                if (resultAtlas != null)
                {
                    resultAtlas = new MultiAtlas(resultAtlas, textMultiAtlas);
                }
                /*
                 * For this case, there was no previous resultAtlas since no binary atlases for
                 * found.
                 */
                else
                {
                    resultAtlas = textMultiAtlas;
                }
            }
        }

        if (this.multiAtlasName != null && resultAtlas != null)
        {
            resultAtlas.setName(this.multiAtlasName);
        }
        return resultAtlas;
    }

    private Atlas loadSingleAtlas(final List<Resource> binaryResources,
            final List<Resource> textResources)
    {
        Atlas result;
        if (!binaryResources.isEmpty())
        {
            result = PackedAtlas.load(binaryResources.get(0));
        }
        else if (!textResources.isEmpty())
        {
            result = new TextAtlasBuilder().read(textResources.get(0));
        }
        else
        {
            throw new CoreException("Could not find any resources to load!");
        }
        if (this.atlasEntityFilter != null)
        {
            final Optional<Atlas> subAtlas = result.subAtlas(this.atlasEntityFilter,
                    AtlasCutType.SOFT_CUT);
            result = subAtlas.orElse(null);
        }
        return result;
    }

    private List<Atlas> loadTextAtlasesFromResources(final List<Resource> textResources)
    {
        final List<Atlas> textAtlases = new ArrayList<>();
        for (final Resource textResource : textResources)
        {
            final Atlas atlas = new TextAtlasBuilder().read(textResource);
            if (this.atlasEntityFilter != null)
            {
                final Optional<Atlas> filtered = atlas.subAtlas(this.atlasEntityFilter,
                        AtlasCutType.SOFT_CUT);
                if (!filtered.isPresent())
                {
                    continue;
                }
                textAtlases.add(filtered.get());
            }
            else
            {
                textAtlases.add(atlas);
            }
        }
        return textAtlases;
    }

    private List<Resource> resourcesIn(final Resource resource)
    {
        final List<Resource> result = new ArrayList<>();
        if (resource != null)
        {
            if (resource instanceof File)
            {
                result.addAll(new AtlasFileSelector().select((File) resource));
            }
            else
            {
                result.add(resource);
            }
        }
        return result;
    }
}
