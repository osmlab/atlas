package org.openstreetmap.atlas.geography.atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load an {@link Atlas} from a {@link Resource} or an {@link Iterable} of {@link Resource}s. Also
 * supports loading based on a resource name filter. To recursively load all {@link Atlas}es in a
 * directory, see the {@link AtlasResourceLoader2#loadRecursively} method.
 *
 * @author lcram
 */
public class AtlasResourceLoader2
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasResourceLoader2.class);
    private static final Predicate<Resource> LOOKS_LIKE_TEXT_ATLAS = resource ->
    {
        if (resource instanceof File && !((File) resource).exists())
        {
            logger.warn("Resource {} was of type File but it could not be found",
                    resource.getName());
            return false;
        }
        return resource.firstLine().equals(TextAtlasBuilder.getNodesHeader());
    };
    private static final Predicate<Resource> LOOKS_LIKE_BINARY_ATLAS = resource ->
    {
        if (resource instanceof File && !((File) resource).exists())
        {
            logger.warn("Resource {} was of type File but it could not be found",
                    resource.getName());
            return false;
        }
        return !resource.firstLine().equals(TextAtlasBuilder.getNodesHeader());
    };

    private final Predicate<Resource> resourceFilter;
    private final Predicate<AtlasEntity> atlasEntityFilter;
    private String multiAtlasName;

    public AtlasResourceLoader2()
    {
        this.resourceFilter = resource -> true;
        this.atlasEntityFilter = null;
    }

    /**
     * Load an {@link Atlas} from the provided {@link Resource}(s). If more than one
     * {@link Resource} is provided, the method will utilize the {@link MultiAtlas} to combine them.
     * This method will ignore any of the provided {@link Resource}s that do not contain a valid
     * binary or text {@link Atlas}.
     *
     * @param resource
     *            the {@link Resource}(s) from which to load
     * @return the loaded {@link Atlas}
     */
    public Atlas load(final Resource... resource)
    {
        return load(Iterables.from(resource));
    }

    /**
     * Load an {@link Atlas} from an {@link Iterable} of {@link Resource}s. If more than one
     * {@link Resource} is provided, the method will utilize the {@link MultiAtlas} to combine them.
     * This method will ignore any of the provided {@link Resource}s that do not contain a valid
     * binary or text {@link Atlas}.
     *
     * @param input
     *            the {@link Iterable} of {@link Resource}s from which to load
     * @return the loaded {@link Atlas}
     */
    public Atlas load(final Iterable<? extends Resource> input)
    {
        final List<Resource> atlasResources = Iterables.stream(input)
                .flatMap(this::upcastAndRemoveNullResources).filter(this.resourceFilter)
                .collectToList();

        /*
         * It would probably be better to throw an exception here, but for backwards-compatibility
         * we will return null instead.
         */
        if (atlasResources.isEmpty())
        {
            return null;
        }
        else if (atlasResources.size() == 1)
        {
            return loadAndFilterAtlasResource(atlasResources.get(0));
        }
        else
        {
            return loadAndFilterMultipleAtlasResources(atlasResources);
        }
    }

    /**
     * Load an {@link Atlas} from the provided {@link File} {@link Resource}(s). If any of the
     * provided {@link File}(s) are directories, the method will recursively descend into the
     * directory and include every {@link Atlas} it discovers. It identifies {@link Atlas}es by
     * looking for {@link FileSuffix#ATLAS}, {@link FileSuffix#TEXT_ATLAS}, and
     * {@link FileSuffix#GZIP_ATLAS} file extensions. Like with the
     * {@link AtlasResourceLoader2#load} method, this method will utilize the {@link MultiAtlas} to
     * combine the {@link Atlas}es.
     *
     * @param file
     *            the {@link File}(s) from which to load
     * @return the loaded {@link Atlas}
     */
    public Atlas loadRecursively(final File... file)
    {
        return loadRecursively(Iterables.from(file));
    }

    /**
     * Load an {@link Atlas} from an {@link Iterable} of {@link File} {@link Resource}s. If any of
     * the provided {@link File}(s) are directories, the method will recursively descend into the
     * directory and include every {@link Atlas} it discovers. It identifies {@link Atlas}es by
     * looking for {@link FileSuffix#ATLAS} {@link FileSuffix#TEXT_ATLAS}, and
     * {@link FileSuffix#GZIP_ATLAS} file extensions. Like with the
     * {@link AtlasResourceLoader2#load} method, this method will utilize the {@link MultiAtlas} to
     * combine the {@link Atlas}es.
     *
     * @param input
     *            the {@link Iterable} of {@link File} {@link Resource}s from which to load
     * @return the loaded {@link Atlas}
     */
    public Atlas loadRecursively(final Iterable<File> input)
    {
        // TODO
        return null;
    }

    private List<Resource> filterForBinaryAtlasResources(final List<Resource> atlasResources)
    {
        return atlasResources.stream().filter(LOOKS_LIKE_BINARY_ATLAS).collect(Collectors.toList());
    }

    private List<Resource> filterForTextAtlasResources(final List<Resource> atlasResources)
    {
        return atlasResources.stream().filter(LOOKS_LIKE_TEXT_ATLAS).collect(Collectors.toList());
    }

    private Atlas loadAndFilterAtlasResource(final Resource resource)
    {
        Atlas result;

        if (resource instanceof File && !((File) resource).exists())
        {
            logger.warn("Resource {} was of type File but it could not be found",
                    resource.getName());
            return null;
        }

        if (resource.firstLine().equals(TextAtlasBuilder.getNodesHeader()))
        {
            result = new TextAtlasBuilder().read(resource);
        }
        else
        {
            try
            {
                result = PackedAtlas.load(resource);
            }
            catch (final Exception exception)
            {
                logger.warn("Failed to load an atlas from {} with name {}",
                        resource.getClass().getName(), resource.getName(), exception);
                return null;
            }
        }
        if (this.atlasEntityFilter != null)
        {
            final Optional<Atlas> subAtlas = result.subAtlas(this.atlasEntityFilter,
                    AtlasCutType.SOFT_CUT);
            result = subAtlas.orElse(null);
        }
        return result;
    }

    private Atlas loadAndFilterMultipleAtlasResources(final List<Resource> atlasResources)
    {
        final List<Resource> binaryResources = filterForBinaryAtlasResources(atlasResources);
        final List<Resource> textResources = filterForTextAtlasResources(atlasResources);

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
            final List<Atlas> textAtlases = loadAndFilterTextAtlasesFromResources(textResources);
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

    private List<Atlas> loadAndFilterTextAtlasesFromResources(
            final List<Resource> textAtlasResources)
    {
        final List<Atlas> textAtlases = new ArrayList<>();
        for (final Resource textResource : textAtlasResources)
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

    private List<Resource> upcastAndRemoveNullResources(final Resource resource)
    {
        final List<Resource> result = new ArrayList<>();
        if (resource != null)
        {
            result.add(resource);
        }
        return result;
    }
}
