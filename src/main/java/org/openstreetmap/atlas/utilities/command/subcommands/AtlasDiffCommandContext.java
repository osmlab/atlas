package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * @author matthieun
 */
public class AtlasDiffCommandContext
{
    private final File beforeAtlasFile;
    private final File afterAtlasFile;
    private final boolean useGeoJson;
    private final boolean useLdGeoJson;
    private final boolean fullText;
    private final Long selectedIdentifier;
    private final ItemType selectedType;
    private final boolean recursive;

    public AtlasDiffCommandContext(final File beforeAtlasFile, final File afterAtlasFile, // NOSONAR
            final boolean useGeoJson, final boolean useLdGeoJson, final boolean fullText,
            final Long selectedIdentifier, final ItemType selectedType, final boolean recursive)
    {
        this.beforeAtlasFile = beforeAtlasFile;
        this.afterAtlasFile = afterAtlasFile;
        this.useGeoJson = useGeoJson;
        this.useLdGeoJson = useLdGeoJson;
        this.fullText = fullText;
        this.selectedIdentifier = selectedIdentifier;
        this.selectedType = selectedType;
        this.recursive = recursive;
    }

    public File getAfterAtlasFile()
    {
        return this.afterAtlasFile;
    }

    public File getBeforeAtlasFile()
    {
        return this.beforeAtlasFile;
    }

    public Long getSelectedIdentifier()
    {
        return this.selectedIdentifier;
    }

    public ItemType getSelectedType()
    {
        return this.selectedType;
    }

    public boolean isFullText()
    {
        return this.fullText;
    }

    public boolean isRecursive()
    {
        return this.recursive;
    }

    public boolean isUseGeoJson()
    {
        return this.useGeoJson;
    }

    public boolean isUseLdGeoJson()
    {
        return this.useLdGeoJson;
    }
}
