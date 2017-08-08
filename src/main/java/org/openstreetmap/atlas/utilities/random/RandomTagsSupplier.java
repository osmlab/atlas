package org.openstreetmap.atlas.utilities.random;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Supplier for generating random name/value pairs for tags
 *
 * @author cstaylor
 */
public class RandomTagsSupplier implements Supplier<Tuple<String, String>>
{
    private final Optional<String> fixedName;

    private final Optional<String[]> fixedValues;

    private final Random random;

    private final RandomTextGenerator textGenerator;

    /**
     * Convenience method that wraps everything up into a Map of name/value pairs
     *
     * @param count
     *            the number of name/value pairs we want to generate
     * @return the map containing the generated name value pairs
     */
    public static Map<String, String> randomTags(final int count)
    {
        return Stream.generate(new RandomTagsSupplier()).limit(count).collect(
                Collectors.toMap(Tuple::getFirst, Tuple::getSecond, (first, second) -> first));
    }

    /**
     * Convenience method that wraps everything up into a Map of name/value pairs
     *
     * @param count
     *            the number of name/value pairs we want to generate
     * @param fixedKey
     *            the tag key
     * @param fixedValues
     *            optional list of values to use instead of completely random text
     * @return the map containing the generated name value pairs
     */
    public static Map<String, String> randomTags(final int count, final String fixedKey,
            final String... fixedValues)
    {
        return Stream.generate(new RandomTagsSupplier(fixedKey, fixedValues)).limit(count).collect(
                Collectors.toMap(Tuple::getFirst, Tuple::getSecond, (first, second) -> first));
    }

    /**
     * Convenience method that wraps everything up into a Map of name/value pairs
     *
     * @param count
     *            the number of name/value pairs we want to generate
     * @param keyToExclude
     *            the key we don't want as part of our output
     * @return the map containing the generated name value pairs
     */
    public static Map<String, String> randomTagsExcluding(final int count,
            final String keyToExclude)
    {
        return Stream.generate(new RandomTagsSupplier())
                .filter(entry -> !entry.getFirst().equals(keyToExclude)).limit(count)
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond,
                        (first, second) -> first));
    }

    public RandomTagsSupplier()
    {
        this(null, null, null);
    }

    public RandomTagsSupplier(final String fixedName)
    {
        this(fixedName, null, null);
    }

    public RandomTagsSupplier(final String fixedName, final String[] fixedValues)
    {
        this(fixedName, fixedValues, null);
    }

    public RandomTagsSupplier(final String fixedName, final String[] fixedValues,
            final Random random)
    {
        this.fixedName = Optional.ofNullable(fixedName);
        this.fixedValues = Optional.ofNullable(fixedValues);
        this.random = random == null ? new SecureRandom() : random;
        this.textGenerator = new RandomTextGenerator(this.random);
    }

    @Override
    public Tuple<String, String> get()
    {
        final String key = this.fixedName.orElse(this.textGenerator.newWord());
        String value = null;
        if (this.fixedValues.isPresent())
        {
            final String[] fixedValueTable = this.fixedValues.get();
            value = fixedValueTable[this.random.nextInt(fixedValueTable.length)];
        }
        else
        {
            value = this.textGenerator.newWord();
        }
        return new Tuple<>(key, value);
    }
}
