package org.openstreetmap.atlas.geography.atlas.packed;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.streaming.CounterOutputStream;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.resource.zip.ZipFileWritableResource;
import org.openstreetmap.atlas.streaming.resource.zip.ZipResource;
import org.openstreetmap.atlas.streaming.resource.zip.ZipResource.ZipIterator;
import org.openstreetmap.atlas.streaming.resource.zip.ZipWritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StreamIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that serializes and deserializes {@link PackedAtlas}s to a {@link ZipResource}
 *
 * @author matthieun
 * @author lcram
 */
public final class PackedAtlasSerializer
{
    /**
     * Exception that is thrown in case a field is in a {@link ZipResource} but the current
     * implementation of the {@link PackedAtlas} does not recognize it.
     *
     * @author matthieun
     */
    private static class MissingFieldException extends CoreException
    {
        private static final long serialVersionUID = 6780849464228478451L;

        MissingFieldException(final String message)
        {
            super(message);
        }

        MissingFieldException(final String message, final Object... items)
        {
            super(message, items);
        }

        MissingFieldException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }

    public static final String META_DATA_ERROR_MESSAGE = "MetaData not here!";
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlasSerializer.class);
    // The fields not serialized.
    private static final StringList EXCLUDED_FIELDS = new StringList(PackedAtlas.FIELD_BOUNDS,
            PackedAtlas.FIELD_SERIAL_VERSION_UID, PackedAtlas.FIELD_LOGGER, "$SWITCH_TABLE$",
            PackedAtlas.FIELD_SERIALIZER, PackedAtlas.FIELD_SAVE_SERIALIZATION_FORMAT,
            PackedAtlas.FIELD_LOAD_SERIALIZATION_FORMAT, PackedAtlas.FIELD_PREFIX,
            /* https://stackoverflow.com/a/39037512/1558687 */"$jacocoData");
    private final PackedAtlas atlas;
    private final ZipResource source;

    /**
     * Use reflection to create a {@link PackedAtlas} from a serialized resource.
     *
     * @param resource
     *            The resource
     * @return The deserialized {@link PackedAtlas}
     */
    protected static PackedAtlas load(final Resource resource)
    {
        // Create an empty Atlas.
        final PackedAtlas atlas = new PackedAtlas();
        // Build the serializer with it
        final PackedAtlasSerializer serializer = new PackedAtlasSerializer(atlas, resource);
        // Assign the serializer to the Atlas! Then the Atlas will load all the fields depending on
        // demand.
        serializer.assign();

        // This is for backwards compatibility and will slow Atlas loading
        determineAtlasLoadFormat(atlas);

        return atlas;
    }

    /*
     * Try loading the meta data to make sure the data format is appropriate. Keep trying formats
     * until we find the right one
     */
    private static void determineAtlasLoadFormat(final PackedAtlas atlas)
    {
        final AtlasSerializationFormat[] possibleFormats = AtlasSerializationFormat.values();
        for (final AtlasSerializationFormat candidateFormat : possibleFormats)
        {
            logger.trace("Trying load format {} for atlas {}", candidateFormat, atlas.getName());
            atlas.setLoadSerializationFormat(candidateFormat);
            try
            {
                atlas.metaData();
            }
            catch (final CoreException exception)
            {
                logger.debug("Load format {} invalid for atlas {}", candidateFormat,
                        atlas.getName(), exception);
                continue;
            }
            // If we make it here, then we found the appropriate format and we can bail out
            logger.trace("Using load format {} for atlas {}", candidateFormat, atlas.getName());
            return;
        }

        throw new CoreException("Could not determine a valid load format for atlas {}",
                atlas.getName());
    }

    /**
     * Construct a new {@link PackedAtlasSerializer}.
     *
     * @param atlas
     *            The {@link Atlas} to be serialized / deserialized
     * @param resource
     *            The resource where to serialize / deserialize from.
     */
    protected PackedAtlasSerializer(final PackedAtlas atlas, final Resource resource)
    {
        this.atlas = atlas;
        if (resource instanceof File && !resource.isGzipped())
        {
            // Make sure to use ZipFileWritableResource to take advantage of the random access.
            this.source = new ZipFileWritableResource((File) resource);
        }
        else if (resource instanceof WritableResource)
        {
            this.source = new ZipWritableResource((WritableResource) resource);
        }
        else
        {
            this.source = new ZipResource(resource);
        }
    }

    /**
     * This method is used by the {@link PackedAtlas} to access its own fields!
     *
     * @param name
     *            The name of the field
     */
    protected void deserializeIfNeeded(final String name)
    {
        final Object member;
        try
        {
            final Field field = readField(name);
            member = getField(field);
            if (member == null)
            {
                if (this.source == null)
                {
                    throw new CoreException(
                            "The PackedAtlasSerializer has not been properly assigned.");
                }
                // If the field is not populated, this will trigger a load (partial or not,
                // depending on the zip resource)
                load(name);
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to read Atlas field {}", name, e);
        }
    }

    /**
     * Save an Atlas file to a {@link ZipWritableResource}. This method uses reflection to identify
     * all the fields in the {@link PackedAtlas}, and stores each field into a separate zip entry,
     * named after the field itself.
     */
    protected void save()
    {
        if (this.source instanceof ZipWritableResource)
        {
            // Load the Atlas completely if it has not been loaded yet
            this.atlas.getSerializer()
                    .ifPresent(PackedAtlasSerializer::deserializeAllFieldsIfNeeded);
            final ZipWritableResource destination = (ZipWritableResource) this.source;
            // Isolate the metaData field
            final Field metaData = readField(PackedAtlas.FIELD_META_DATA);
            final Iterable<Resource> firstResource = Iterables.from(fieldTranslator(metaData));
            final Iterable<Resource> fieldResources = fields().filter(field ->
            {
                final String fieldName = field.getName();
                return !PackedAtlas.FIELD_META_DATA.equals(fieldName)
                        && !EXCLUDED_FIELDS.startsWithContains(fieldName);
            }).map(this::fieldTranslator).collect();
            // Put the metaData field first, always.
            final Iterable<Resource> result = new MultiIterable<>(firstResource, fieldResources);
            destination.writeAndClose(result);
        }
        else
        {
            throw new CoreException("The ZipResource {} is not writable.", this.source);
        }
    }

    /**
     * Assign itself as the Atlas' official serializer
     */
    private void assign()
    {
        setField(readField(PackedAtlas.FIELD_SERIALIZER), this);
    }

    /**
     * @return True if the underlying {@link Resource} allows for random access to the serialized
     *         fields.
     */
    private boolean canLoadWithRandomAccess()
    {
        return this.source instanceof ZipFileWritableResource;
    }

    private OutputStream compress(final OutputStream out) throws IOException
    {
        return out;
    }

    private InputStream decompress(final InputStream input) throws IOException
    {
        return input;
    }

    private void deserializeAllFields()
    {
        Iterables.stream(this.source.entries()).forEach(resource ->
        {
            final String name = resource.getName();
            try
            {
                final Field field = readField(name);
                final Object value = deserializeResource(resource, name);
                setField(field, value);
            }
            catch (final MissingFieldException e)
            {
                // Skipping field, comes from a legacy serialized file. We however have to read it
                // fully to move to the next one. Here we skip the selection logic of
                // deserializeResource and just force Java deserialization
                deserializeJavaResource(resource);
            }
        });
    }

    /**
     * Go after all the fields that might not have been deserialized and deserialize them
     */
    private void deserializeAllFieldsIfNeeded()
    {
        fields().map(Field::getName).forEach(this::deserializeIfNeeded);
    }

    private Object deserializeJavaResource(final Resource resource)
    {
        try (ObjectInputStream input = new ObjectInputStream(decompress(resource.read())))
        {
            final Time start = Time.now();
            final Object result = input.readObject();
            Streams.close(input);
            logger.trace("Loaded Field {} from {} in {}", resource.getName(), this.source,
                    start.elapsedSince());
            return result;
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not load Field {} from {}", resource.getName(),
                    this.source, e);
        }
    }

    private Object deserializeProtoResource(final Resource resource, final String fieldName)
    {
        final Field field = readField(fieldName);
        final Class<?> fieldClass = field.getType();
        Constructor<?> fieldClassConstructor = null;

        // We need to obtain a dummy instance of the field we want to deserialize. We then use this
        // dummy instance as a handle to get the correct {@link ProtoAdapter}.
        try
        {
            fieldClassConstructor = fieldClass.getDeclaredConstructor();
        }
        catch (final Exception exception)
        {
            throw new CoreException("Class {} does not implement a nullary constructor",
                    fieldClass.getName(), exception);
        }
        fieldClassConstructor.setAccessible(true);

        Object handle = null;
        try
        {
            handle = fieldClassConstructor.newInstance();
        }
        catch (final Exception exception)
        {
            throw new CoreException("Failed to create instance of {}", fieldClass.getName(),
                    exception);
        }

        ProtoSerializable protoHandle = null;
        try
        {
            protoHandle = (ProtoSerializable) handle;
        }
        catch (final ClassCastException exception)
        {
            throw new CoreException("{} is not ProtoSerializable", fieldClass.getName(), exception);
        }

        final ProtoAdapter adapter = protoHandle.getProtoAdapter();
        final ProtoSerializable deserializedMember = adapter
                .deserialize(resource.readBytesAndClose());

        return deserializedMember;
    }

    private Object deserializeResource(final Resource resource, final String fieldName)
    {
        final AtlasSerializationFormat loadFormat = this.atlas.getLoadSerializationFormat();
        Object result = null;
        switch (loadFormat)
        {
            case JAVA:
                result = deserializeJavaResource(resource);
                break;
            case PROTOBUF:
                result = deserializeProtoResource(resource, fieldName);
                break;
            default:
                throw new CoreException("Unsupported serialization format {}",
                        loadFormat.toString());
        }
        if (result == null)
        {
            throw new CoreException("Unable to deserialize field {} from resource {} in {}.",
                    fieldName, resource.getName(), this.atlas.getName());
        }
        return result;
    }

    /**
     * Deserialize a specific field and assign it to the Atlas.
     *
     * @param name
     *            The name of the field.
     */
    private void deserializeSingleField(final String name)
    {
        final Object result;
        if (canLoadWithRandomAccess())
        {
            final Resource resource = ((ZipFileWritableResource) this.source).entryForName(name);
            result = deserializeResource(resource, name);
        }
        else if (PackedAtlas.FIELD_META_DATA.equals(name))
        {
            // The metaData field is always the first.
            final Iterable<Resource> resources = this.source.entries();
            try (ZipIterator iterator = (ZipIterator) resources.iterator())
            {
                final Resource resource = iterator.next();
                if (resource == null)
                {
                    throw new CoreException(META_DATA_ERROR_MESSAGE);
                }
                result = deserializeResource(resource, name);
            }
        }
        else
        {
            throw new CoreException(
                    "Cannot deserialize a specific field without a ZipFileWritableResource");
        }
        setField(readField(name), result);
    }

    /**
     * The function that translates a reflection {@link Field} into a {@link Resource}
     *
     * @param field
     *            The field
     * @return The resource
     */
    private Resource fieldTranslator(final Field field)
    {
        final AtlasSerializationFormat saveFormat = this.atlas.getSaveSerializationFormat();

        switch (saveFormat)
        {
            case JAVA:
                final Object objectCandidate = getField(field);
                return makeJavaResource(objectCandidate, field.getName());
            case PROTOBUF:
                final ProtoSerializable protoCandidate = (ProtoSerializable) getField(field);
                return makeProtoResource(protoCandidate, field.getName());
            default:
                throw new CoreException("Unsupported serialization format {}",
                        saveFormat.toString());
        }
    }

    private StreamIterable<Field> fields()
    {
        return Iterables.stream(Iterables.from(PackedAtlas.class.getDeclaredFields()))
                .filter(field -> !EXCLUDED_FIELDS.startsWithContains(field.getName())).map(field ->
                {
                    field.setAccessible(true);
                    return field;
                });
    }

    private Object getField(final Field field)
    {
        try
        {
            return field.get(this.atlas);
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to access field {} for {}", field.getName(),
                    this.atlas.getName(), e);
        }
    }

    /**
     * De-serialize a specific field and set it to the Atlas.
     *
     * @param name
     *            The name of the field.
     */
    private void load(final String name)
    {
        if (canLoadWithRandomAccess() || PackedAtlas.FIELD_META_DATA.equals(name))
        {
            deserializeSingleField(name);
        }
        else
        {
            deserializeAllFields();
        }
    }

    /**
     * Transform a field of this Atlas into a readable {@link Resource}. The underlying
     * implementation stores everything in a {@link ByteArrayResource}
     *
     * @param field
     *            The field to translate
     * @param name
     *            The name of the resource
     * @return The resource
     */
    private Resource makeJavaResource(final Object field, final String name)
    {
        // First pass read, to count the size
        final CounterOutputStream counterOutputStream = new CounterOutputStream();
        try (ObjectOutputStream outCounter = new ObjectOutputStream(
                compress(new BufferedOutputStream(counterOutputStream))))
        {
            outCounter.writeObject(field);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not count the size of {}.", field, e);
        }
        final long count = counterOutputStream.getCount();

        // Second pass, write to the memory resource.
        final ByteArrayResource resource = new ByteArrayResource(count).withName(name);
        logger.trace("Saving field {}", resource.getName());
        if (field == null)
        {
            logger.warn("Field {} is null in atlas {} of size {}", name, this.atlas.getName(),
                    this.atlas.size());
            return resource;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(
                compress(new BufferedOutputStream(resource.write()))))
        {
            out.writeObject(field);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not convert {} to a readable resource.", field, e);
        }
        return resource;
    }

    private Resource makeProtoResource(final ProtoSerializable field, final String name)
    {
        // We automatically get the correct adapter for whatever type 'field' happens to be
        final ProtoAdapter adapter = field.getProtoAdapter();
        // The adapter handles all the actual serialization using the protobuf classes. Easy!
        final byte[] byteContents = adapter.serialize(field);

        final ByteArrayResource resource = new ByteArrayResource(byteContents.length)
                .withName(name);

        try (BufferedOutputStream out = new BufferedOutputStream(resource.write()))
        {
            out.write(byteContents);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not convert {} to a readable resource.", field, e);
        }
        return resource;
    }

    private Field readField(final String name) throws MissingFieldException
    {
        try
        {
            final Field result = PackedAtlas.class.getDeclaredField(name);
            result.setAccessible(true);
            return result;
        }
        catch (final NoSuchFieldException e)
        {
            logger.warn("Unable to access field {}", name);
            throw new MissingFieldException("Unable to access field {}", name, e);
        }
    }

    /**
     * Assign a field to the Atlas
     *
     * @param field
     *            The field to assign
     * @param object
     *            The object to assign to the field.
     */
    private void setField(final Field field, final Object object)
    {
        try
        {
            field.set(this.atlas, object);
        }
        catch (final Exception e)
        {
            throw new CoreException("Cannot set field {} for Atlas {}", field.getName(),
                    this.atlas.getName(), e);
        }
    }
}
