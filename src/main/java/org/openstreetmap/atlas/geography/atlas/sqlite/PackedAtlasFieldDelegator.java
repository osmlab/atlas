package org.openstreetmap.atlas.geography.atlas.sqlite;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedTagStore;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;
import org.openstreetmap.atlas.utilities.maps.LongToLongMap;

/**
 * This delegator allows the {@link SQLiteWriter} to access internal {@link PackedAtlas} fields it
 * needs to efficiently build the database.
 *
 * @author lcram
 */
public class PackedAtlasFieldDelegator
{
    // TODO this class has a lot of fields. It may be worth refactoring it out and just using
    // reflection. Alternatively, we could also just make a ton of PackedAtlas fields public. Let's
    // discuss these options please.

    private IntegerDictionary<String> dictionaryHandle;
    private PackedTagStore nodeTagsHandle;
    private PackedTagStore edgeTagsHandle;
    private PackedTagStore pointTagsHandle;
    private PackedTagStore lineTagsHandle;
    private PackedTagStore areaTagsHandle;
    private LongToLongMap nodeIdentifierMapHandle;
    private LongToLongMap edgeIdentifierMapHandle;
    private LongToLongMap pointIdentifierMapHandle;
    private LongToLongMap lineIdentifierMapHandle;
    private LongToLongMap areaIdentifierMapHandle;

    public LongToLongMap areaIdentifierMapHandle()
    {
        return this.areaIdentifierMapHandle;
    }

    public PackedTagStore areaTagsHandle()
    {
        return this.areaTagsHandle;
    }

    public IntegerDictionary<String> dictionaryHandle()
    {
        return this.dictionaryHandle;
    }

    public LongToLongMap edgeIdentifierMapHandle()
    {
        return this.edgeIdentifierMapHandle;
    }

    public PackedTagStore edgeTagsHandle()
    {
        return this.edgeTagsHandle;
    }

    public LongToLongMap lineIdentifierMapHandle()
    {
        return this.lineIdentifierMapHandle;
    }

    public PackedTagStore lineTagsHandle()
    {
        return this.lineTagsHandle;
    }

    public LongToLongMap nodeIdentifierMapHandle()
    {
        return this.nodeIdentifierMapHandle;
    }

    public PackedTagStore nodeTagsHandle()
    {
        return this.nodeTagsHandle;
    }

    public LongToLongMap pointIdentifierMapHandle()
    {
        return this.pointIdentifierMapHandle;
    }

    public PackedTagStore pointTagsHandle()
    {
        return this.pointTagsHandle;
    }

    public PackedAtlasFieldDelegator withAreaIdentifierMap(final LongToLongMap map)
    {
        this.areaIdentifierMapHandle = map;
        return this;
    }

    public PackedAtlasFieldDelegator withAreaTags(final PackedTagStore tags)
    {
        this.areaTagsHandle = tags;
        return this;
    }

    public PackedAtlasFieldDelegator withDictionary(final IntegerDictionary<String> dictionary)
    {
        this.dictionaryHandle = dictionary;
        return this;
    }

    public PackedAtlasFieldDelegator withEdgeIdentifierMap(final LongToLongMap map)
    {
        this.edgeIdentifierMapHandle = map;
        return this;
    }

    public PackedAtlasFieldDelegator withEdgeTags(final PackedTagStore tags)
    {
        this.edgeTagsHandle = tags;
        return this;
    }

    public PackedAtlasFieldDelegator withLineIdentifierMap(final LongToLongMap map)
    {
        this.lineIdentifierMapHandle = map;
        return this;
    }

    public PackedAtlasFieldDelegator withLineTags(final PackedTagStore tags)
    {
        this.lineTagsHandle = tags;
        return this;
    }

    public PackedAtlasFieldDelegator withNodeIdentifierMap(final LongToLongMap map)
    {
        this.nodeIdentifierMapHandle = map;
        return this;
    }

    public PackedAtlasFieldDelegator withNodeTags(final PackedTagStore tags)
    {
        this.nodeTagsHandle = tags;
        return this;
    }

    public PackedAtlasFieldDelegator withPointIdentifierMap(final LongToLongMap map)
    {
        this.pointIdentifierMapHandle = map;
        return this;
    }

    public PackedAtlasFieldDelegator withPointTags(final PackedTagStore tags)
    {
        this.pointTagsHandle = tags;
        return this;
    }
}
