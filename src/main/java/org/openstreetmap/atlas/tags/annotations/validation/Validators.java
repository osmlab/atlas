package org.openstreetmap.atlas.tags.annotations.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.LocalizedTagNameWithOptionalDate;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.Taggable.TagSearchOption;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.TagValueAs;
import org.openstreetmap.atlas.tags.cache.CachingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * Builds a table of {@link TagValidator}s using Java annotations and introspection.
 *
 * @author cstaylor
 */
public class Validators
{
    /**
     * Extracted the code for finding the tag key into its own public inner class
     *
     * @author cstaylor
     */
    public static final class TagKeySearch
    {
        public static Optional<TagKeySearchResults> findTagKeyIn(final Class<?> tagClass)
        {
            final Tag tag = tagClass.getDeclaredAnnotation(Tag.class);
            for (final Field field : tagClass.getDeclaredFields())
            {
                final TagKey tagKey = field.getAnnotation(TagKey.class);
                if (tagKey != null && field.getType().isAssignableFrom(String.class))
                {
                    try
                    {
                        final String returnValue = (String) field.get(null);
                        if (returnValue == null || returnValue.trim().length() == 0)
                        {
                            throw new IllegalArgumentException(
                                    String.format("%s is missing a key", tagClass.getName()));
                        }
                        return Optional.of(new TagKeySearchResults(tag, tagKey, returnValue));
                    }
                    catch (final IllegalAccessException oops)
                    {
                        throw new IllegalArgumentException(String.format(
                                "Check the source code for %s: the @TagKey is probably not a public static final String constant",
                                tagClass.getName()), oops);
                    }
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Immutable object capturing the results of a tag key search
     *
     * @author cstaylor
     */
    public static final class TagKeySearchResults
    {
        private final Tag tag;
        private final TagKey key;
        private final String keyName;

        private TagKeySearchResults(final Tag tag, final TagKey key, final String keyName)
        {
            this.tag = tag;
            this.key = key;
            this.keyName = keyName;
        }

        public String getKeyName()
        {
            return this.keyName;
        }

        public Tag getTag()
        {
            return this.tag;
        }

        public TagKey getTagKey()
        {
            return this.key;
        }
    }

    /**
     * Single point for handling both standard and localizable tags, simplifying the main Validators
     * source code below
     *
     * @author cstaylor
     */
    private static final class ValidatorMap
    {
        private final Map<String, TagValidator> validators;
        private final Map<String, TagValidator> localizedValidators;
        private final Map<String, Class<?>> origins;

        ValidatorMap()
        {
            this.validators = new HashMap<>();
            this.localizedValidators = new HashMap<>();
            this.origins = new HashMap<>();
        }

        boolean canValidate(final String name)
        {
            return validatorFor(name) != null;
        }

        Class<?> classFor(final String name)
        {
            return this.origins.get(name);
        }

        void put(final Class<?> tagClass, final String name, final TagKey tagKey,
                final TagValidator validator)
        {
            if (tagKey.value() == KeyType.EXACT)
            {
                this.validators.put(name, validator);
            }
            else
            {
                // This is for localized tags
                this.localizedValidators.put(name, validator);
            }
            this.origins.put(name, tagClass);
        }

        TagValidator validatorFor(final String name)
        {
            // Step 1: Check standard exact name match validators
            TagValidator validator = this.validators.get(name);
            if (validator == null)
            {
                final LocalizedTagNameWithOptionalDate localizedName = new LocalizedTagNameWithOptionalDate(
                        name);
                validator = this.localizedValidators.get(localizedName.getName());
            }
            return validator;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Validators.class);

    private final ValidatorMap validators;

    private final EnumMap<Validation, Class<? extends TagValidator>> validatorTypes;

    public static String findTagNameIn(final Class<?> tagClass)
    {
        final Optional<TagKeySearchResults> tagName = TagKeySearch.findTagKeyIn(tagClass);
        if (tagName.isPresent())
        {
            return tagName.get().getKeyName();
        }
        throw new IllegalArgumentException(
                String.format("key must be declared in class: %s", tagClass.getName()));
    }

    /**
     * Helpful method for swizzling an interface Tag that includes the contents of an enum tag
     * through the [with] annotation feature into its possible value if that value is found in the
     * passed in Taggable parameter and the enumType provided is actually listed in the [with]
     * attribute.
     * <p>
     * See the FromEnumTestCase class for an example of how to use this method
     *
     * @param <T>
     *            the type of enum tag we're parsing
     * @param tagType
     *            the interface tag with a [with] attribute that we want a possible value from
     * @param enumType
     *            the return value type we want a value from
     * @param taggable
     *            the source of tags and their values
     * @return an empty optional if the tagType isn't a tag, doesn't have a key, enumType is not
     *         included in tagType's [with] list, the value isn't found in taggable, or no enum
     *         value in enumType matches (ignoring case) the tag's value
     */
    public static <T extends Enum<T>> Optional<T> from(final Class<?> tagType,
            final Class<T> enumType, final Taggable taggable)
    {
        final Tag tag = tagType.getDeclaredAnnotation(Tag.class);
        if (tag != null)
        {
            if (Stream.of(tag.with()).anyMatch(possible -> possible == enumType))
            {
                return fromHelper(findTagNameIn(tagType), enumType, taggable);
            }
        }
        return Optional.empty();
    }

    /**
     * Caching version - use in generic applications.
     * <p>
     * Helpful method for swizzling an Enum Tag into its possible value if that value is found in
     * the passed in Taggable parameter. This cuts down on a lot of duplicate code that we had in
     * each enum-type Tag.
     * <p>
     * See the FromEnumTestCase class for an example of how to use this method
     *
     * @param <T>
     *            the type of enum tag we're parsing
     * @param tagType
     *            the enum style tag that we want a possible value from
     * @param taggable
     *            the source of tags and their values
     * @return an empty optional if the enum isn't a tag, doesn't have a key, the value isn't found
     *         in taggable, or no enum value matches (ignoring case) the tag's value
     */
    public static <T extends Enum<T>> Optional<T> from(final Class<T> tagType,
            final Taggable taggable)
    {
        return CachingValidator.getInstance().from(tagType, taggable);
    }

    /**
     * Reflection version - use when you need to get a few tags, and no caching is necessary. This
     * method is used by the caching version to populate the cache.
     * <p>
     * Helpful method for swizzling an Enum Tag into its possible value if that value is found in
     * the passed in Taggable parameter. This cuts down on a lot of duplicate code that we had in
     * each enum-type Tag.
     * <p>
     *
     * @param <T>
     *            the type of enum tag we're parsing
     * @param tagType
     *            the enum style tag that we want a possible value from
     * @param taggable
     *            the source of tags and their values
     * @return an empty optional if the enum isn't a tag, doesn't have a key, the value isn't found
     *         in taggable, or no enum value matches (ignoring case) the tag's value
     */
    public static <T extends Enum<T>> Optional<T> fromAnnotation(final Class<T> tagType,
            final Taggable taggable)
    {
        if (tagType.getDeclaredAnnotation(Tag.class) != null)
        {
            return fromHelper(findTagNameIn(tagType), tagType, taggable);
        }
        return Optional.empty();
    }

    /**
     * Convenience method for checking if a class is actually a tag and that its key is localizable
     *
     * @param tagType
     *            the tag class we want to check
     * @return true if tagType is actually a tag and its key is localizable
     */
    public static boolean hasLocalizedTagKey(final Class<?> tagType)
    {
        /*
         * First, sanity check the key
         */
        if (tagType == null)
        {
            throw new IllegalArgumentException("tagType can't be null");
        }
        /*
         * Next, check if the key is actually a key
         */
        final Optional<TagKeySearchResults> tagKey = Validators.TagKeySearch.findTagKeyIn(tagType);
        if (!tagKey.isPresent())
        {
            throw new IllegalArgumentException(
                    String.format("%s isn't a known key", tagType.getName()));
        }
        return tagKey.get().getTagKey().value() == KeyType.LOCALIZED;
    }

    /**
     * Convenience method that returns a filter for a {@link Taggable} that evaluates if all passed
     * in tag types are present
     *
     * @param tagTypes
     *            the type of tags to check
     * @return a filter for a Taggable entity that evaluates if if contains all specified tag types
     */
    public static Predicate<Taggable> hasValuesFor(final Class<?>... tagTypes)
    {
        if (tagTypes.length == 0)
        {
            return taggable -> false;
        }
        return taggable ->
        {
            return hasValuesFor(taggable, tagTypes);
        };
    }

    /**
     * Convenience method for checking if we have defined values for all tags passed into the method
     *
     * @param taggable
     *            where we look up the tags
     * @param tagTypes
     *            the type of tags to check
     * @return true if all of the tags have values, false if at least one is missing
     */
    public static boolean hasValuesFor(final Taggable taggable, final Class<?>... tagTypes)
    {
        for (final Class<?> tagType : tagTypes)
        {
            if (!taggable.getTag(findTagNameIn(tagType)).isPresent())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Use this method to check if a tag exists in a Taggable object _and_ is _not_ one of several
     * provided values. While we could do the same with an EnumSet, this makes calling code cleaner
     * since they only need a single line to check for the existence of at least one item in a set
     * of tag values
     *
     * @param <T>
     *            the enum-type tag's class object
     * @param taggable
     *            where the tags should be read from
     * @param type
     *            the class of the enum-type tag we are looking for?
     * @param values
     *            which values do we want to check against?
     * @return true if the tag exists in taggable and if the value is not any of the specified
     *         values (like an enumset)
     */
    public static <T extends Enum<T>> boolean isNotOfType(final Taggable taggable,
            final Class<T> type, @SuppressWarnings("unchecked") final T... values)
    {
        final Optional<T> possibleRealValue = Validators.from(type, taggable);
        if (!possibleRealValue.isPresent())
        {
            return false;
        }

        final T realValue = possibleRealValue.get();
        for (final T searching : values)
        {
            if (realValue == searching)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Use this method to check if a tag exists in a {@link Taggable} object _and_ is one of several
     * expected values. While we could do the same with an EnumSet, this makes calling code cleaner
     * since they only need a single line to check for the existence of at least one item in a set
     * of tag values
     *
     * @param <T>
     *            the enum-type tag's class object
     * @param taggable
     *            where the tags should be read from
     * @param type
     *            the class of the enum-type tag we are looking for?
     * @param values
     *            which values do we want to check against?
     * @return true if the tag exists in taggable and if the value matches any of the specified
     *         values (like an enumset)
     */
    public static <T extends Enum<T>> boolean isOfType(final Taggable taggable, final Class<T> type,
            @SuppressWarnings("unchecked") final T... values)
    {
        final Optional<T> possibleRealValue = Validators.from(type, taggable);
        if (!possibleRealValue.isPresent())
        {
            return false;
        }

        final T realValue = possibleRealValue.get();
        for (final T searching : values)
        {
            if (realValue == searching)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method for creating the localized name of a tag if and only if tagType is a Tag
     * and the TagKey is localizable
     *
     * @param tagType
     *            check this class if it's a localizable tag and return the localized name
     * @param language
     *            the optional language to localize
     * @param searchOptions
     *            optional arguments that change how we interpret tags
     * @return an optional string of the localized tag name
     */
    public static Optional<String> localizeKeyName(final Class<?> tagType,
            final Optional<IsoLanguage> language, final TagSearchOption... searchOptions)
    {
        /*
         * First, sanity check the key
         */
        if (tagType == null)
        {
            throw new IllegalArgumentException("tagType can't be null");
        }
        /*
         * Next, check if the key is actually a key
         */
        final Optional<TagKeySearchResults> tagKey = Validators.TagKeySearch.findTagKeyIn(tagType);
        if (!tagKey.isPresent())
        {
            throw new IllegalArgumentException(
                    String.format("%s isn't a known key", tagType.getName()));
        }
        final EnumSet<TagSearchOption> searchOptionSet = searchOptions.length > 0
                ? EnumSet.copyOf(Arrays.asList(searchOptions))
                : EnumSet.noneOf(TagSearchOption.class);

        final TagKeySearchResults data = tagKey.get();
        Optional<String> value = Optional.empty();
        if (language.isPresent() && (data.getTagKey().value() == KeyType.LOCALIZED
                || searchOptionSet.contains(TagSearchOption.FORCE_ALL_LOCALIZED_ONLY)))
        {
            value = Optional.of(
                    String.format("%s:%s", data.getKeyName(), language.get().getLanguageCode()));
        }
        if (!value.isPresent())
        {
            value = Optional.of(data.getKeyName());
        }
        return value;
    }

    /**
     * Simple utility method for creating a hashmap out of a set of enum-type tags.
     * <p>
     * The key portion of each map entry is extracted from the tag type itself.
     *
     * @param values
     *            the enum-type tags we want to convert into a hashmap
     * @return the completed hashmap
     */
    public static Map<String, String> toMap(final Enum<?>... values)
    {
        return Arrays.asList(values).stream().collect(Collectors.toMap(
                value -> findTagNameIn(value.getClass()), value -> value.name().toLowerCase()));
    }

    /**
     * Shamelessly taken from SO:
     * http://stackoverflow.com/questions/7254126/get-annotations-for-enum-type-variable
     *
     * @param constant
     *            the particular enum constant we're interested in converting
     * @return the value of the constant or the overriden value from the TagValueAs annotation
     */
    private static String enumConstantToValue(final Enum<?> constant)
    {
        try
        {
            final Field field = constant.getDeclaringClass().getField(constant.name());
            final TagValueAs substitutedValue = field.getAnnotation(TagValueAs.class);
            final String returnValue = substitutedValue == null
                    ? ((Enum<?>) field.get(null)).name().toLowerCase() : substitutedValue.value();
            return returnValue;
        }
        catch (final IllegalAccessException | NoSuchFieldException oops)
        {
            throw new CoreException("{} can't access field value", constant, oops);
        }
    }

    private static <T extends Enum<T>> Optional<T> fromHelper(final String tagName,
            final Class<T> enumType, final Taggable taggable)
    {
        // First try simple match
        // If it fails, try matching based on annotations

        if (tagName != null)
        {
            final Optional<String> tagValue = taggable.getTag(tagName);
            if (tagValue.isPresent())
            {
                final String internedValue = tagValue.get().toUpperCase().intern();
                try
                {
                    final T enumValue = Enum.valueOf(enumType, internedValue);
                    return Optional.of(enumValue);
                }
                catch (final IllegalArgumentException badArgument)
                {
                    // There is no direct name match
                    return fromMatchingHelper(tagName, enumType, internedValue);
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Optional<T> fromMatchingHelper(final String tagName,
            final Class<T> enumType, final String internedValue)
    {
        try
        {
            for (final T enumValue : (T[]) enumType.getMethod("values").invoke(null))
            {
                if (matches(enumValue, internedValue))
                {
                    return Optional.of(enumValue);
                }
            }
        }
        catch (final NoSuchMethodException | IllegalAccessException
                | InvocationTargetException anImpossibleError)
        {
            logger.error("{} doesn't have a values method, or it couldn't be called: impossible",
                    tagName, anImpossibleError);
        }

        return Optional.empty();
    }

    private static boolean matches(final Enum<?> constant, final String value)
    {
        try
        {
            final Field field = constant.getDeclaringClass().getField(constant.name());
            final TagValueAs substitutedValue = field.getAnnotation(TagValueAs.class);
            final String comparisonString = substitutedValue != null ? substitutedValue.value()
                    : constant.name();
            return comparisonString.toUpperCase().intern() == value.intern();
        }
        catch (final NoSuchFieldException oops)
        {
            throw new CoreException("{} can't access field value", constant, oops);
        }
    }

    public Validators(final Class<?> childrenOf)
    {
        this(childrenOf.getPackage().getName());
    }

    public Validators(final String packageName)
    {
        this.validatorTypes = new EnumMap<>(Validation.class);
        this.validators = new ValidatorMap();
        fillValidatorTypes(this.validatorTypes);
        final List<Class<?>> klasses = new ArrayList<>();
        new FastClasspathScanner(packageName).matchClassesWithAnnotation(Tag.class, klasses::add)
                .scan();
        klasses.stream().forEach(this::processClass);
    }

    /**
     * Is key a known tag?
     *
     * @param key
     *            the type of tag we want to verify
     * @return true if we can handle [key] tags, false otherwise
     */
    public boolean canValidate(final String key)
    {
        return this.validators.canValidate(key);
    }

    /**
     * Sometimes we want to know where a tag found by Validators was defined.
     *
     * @param tag
     *            the name of the tag we're searching for
     * @return an Optional containing the class reference if it exists, an empty optional otherwise
     */
    public Optional<Class<?>> findClassDefining(final String tag)
    {
        return Optional.ofNullable(this.validators.classFor(tag));
    }

    public Optional<String> getTagInfo(final String tagName)
    {
        final Class<?> tagClass = this.validators.classFor(tagName);

        if (tagClass != null)
        {
            final Tag tag = tagClass.getAnnotation(Tag.class);
            if (tag.taginfo().equals(""))
            {
                return Optional.empty();
            }
            else
            {
                return Optional.of(tag.taginfo());
            }
        }

        return Optional.empty();
    }

    /**
     * Get the validator used for verifying tags named tagName
     *
     * @param key
     *            the name of the tag we want to verify
     * @return the validator if we support it, null otherwise
     */
    public TagValidator getValidatorFor(final String key)
    {
        return this.validators.validatorFor(key);
    }

    /**
     * Convenience method for checking and verifying a value without having to chain calls
     *
     * @param key
     *            the key we want to verify
     * @param value
     *            the value we want to verify against the allowed values for key
     * @return true if the value is valid for key, false otherwise
     */
    public boolean isValidFor(final String key, final String value)
    {
        return canValidate(key) && getValidatorFor(key).isValid(value);
    }

    protected void fillValidatorTypes(
            final EnumMap<Validation, Class<? extends TagValidator>> validatorTypes)
    {
        validatorTypes.put(Validation.DOUBLE, DoubleValidator.class);
        validatorTypes.put(Validation.LONG, LongValidator.class);
        validatorTypes.put(Validation.MATCH, ExactMatchValidator.class);
        validatorTypes.put(Validation.TIMESTAMP, TimestampValidator.class);
        validatorTypes.put(Validation.NON_EMPTY_STRING, NonEmptyStringValidator.class);
        validatorTypes.put(Validation.NONE, NoneValidator.class);
        validatorTypes.put(Validation.ISO3_COUNTRY, ISO3CountryValidator.class);
        validatorTypes.put(Validation.ISO2_COUNTRY, ISO2CountryValidator.class);
        validatorTypes.put(Validation.ORDINAL, OrdinalValidator.class);
        validatorTypes.put(Validation.URI, URIValidator.class);
        validatorTypes.put(Validation.SPEED, SpeedValidator.class);
        validatorTypes.put(Validation.LENGTH, LengthValidator.class);
    }

    private TagValidator createValidatorFor(final Validation validation)
    {
        final Class<? extends TagValidator> validatorClass = this.validatorTypes.get(validation);
        if (validatorClass == null)
        {
            throw new IllegalArgumentException(
                    String.format("%s is an unsupported validator", validation));
        }
        try
        {
            return validatorClass.newInstance();
        }
        catch (final IllegalAccessException | InstantiationException oops)
        {
            throw new IllegalArgumentException(
                    String.format("%s is an unsupported validator", validation), oops);
        }
    }

    private TagValidator fillEnumerationValues(final ExactMatchValidator validator,
            final Class<?> tagClass)
    {
        for (final Enum<?> enumValue : (Enum<?>[]) tagClass.getEnumConstants())
        {
            validator.withValues(enumConstantToValue(enumValue));
        }
        return validator;
    }

    private void fillExactMatches(final ExactMatchValidator validator, final Field[] fields)
    {
        for (final Field field : fields)
        {
            final TagValue tagValue = field.getAnnotation(TagValue.class);
            if (tagValue != null)
            {
                if (field.getType().isAssignableFrom(String.class))
                {
                    try
                    {
                        final String returnValue = (String) field.get(null);
                        if (returnValue == null || returnValue.trim().length() == 0)
                        {
                            throw new IllegalArgumentException("key can't be empty");
                        }
                        switch (tagValue.value())
                        {
                            case REGEX:
                                validator.withRegularExpressions(returnValue);
                                break;
                            case EXACT:
                                validator.withValues(returnValue);
                                break;
                            default:
                                throw new IllegalStateException(String.format(
                                        "%s is an unsupported value type", tagValue.value()));
                        }
                    }
                    catch (final IllegalAccessException oops)
                    {
                        throw new IllegalArgumentException(oops);
                    }
                }
            }
        }
    }

    private void processClass(final Class<?> tagClass)
    {
        final Tag tag = tagClass.getAnnotation(Tag.class);
        if (tag != null)
        {
            // I've only seen tags being null under eclipse when they mark
            // enums. I think this is an Eclipse bug.
            // This shouldn't be affected by our standard build

            final TagValidator validator = createValidatorFor(tag.value());
            TagKeySearch.findTagKeyIn(tagClass).ifPresent(results ->
            {
                if (validator instanceof ExactMatchValidator)
                {
                    final ExactMatchValidator exactMatch = (ExactMatchValidator) validator;
                    /**
                     * If our validator also supports direct lookup of valid values, fill them here
                     */
                    fillExactMatches(exactMatch, tagClass.getDeclaredFields());
                    if (tagClass.isEnum())
                    {
                        fillEnumerationValues(exactMatch, tagClass);
                    }
                    /**
                     * With classes let us share enum values
                     */
                    for (final Class<? extends Enum<?>> withClass : tag.with())
                    {
                        fillEnumerationValues(exactMatch, withClass);
                    }
                }
                if (validator instanceof NumericValidator)
                {
                    final NumericValidator numeric = (NumericValidator) validator;
                    numeric.setRange(tag.range().min(), tag.range().max());
                    for (final long excludeMe : tag.range().exclude())
                    {
                        numeric.excludeValue(excludeMe);
                    }
                }
                this.validators.put(tagClass, results.getKeyName(), results.getTagKey(), validator);
            });
        }
    }
}
