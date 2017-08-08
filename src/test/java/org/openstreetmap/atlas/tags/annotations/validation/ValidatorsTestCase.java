package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.EnumMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * Comprehensive test case for checking the code coverage within the Validators class
 *
 * @author cstaylor
 */
public class ValidatorsTestCase
{
    /**
     * Sample tag with double validation
     *
     * @author cstaylor
     */
    @Tag(Validation.DOUBLE)
    public interface ValidatorsTestTag
    {
        @TagKey
        String KEY = "abc";
    }

    /**
     * Illegal validator implementation because it has a private constructor
     *
     * @author cstaylor
     */
    private static final class IllegalValidator implements TagValidator
    {
        private IllegalValidator()
        {

        }

        @Override
        public boolean isValid(final String value)
        {
            return true;
        }

    }

    /**
     * Validators implementation assigning the illegal double validator listed above so we can test
     * one of the exception code paths
     *
     * @author cstaylor
     */
    private static final class TestingIllegalValidators extends Validators
    {
        TestingIllegalValidators(final Class<?> packageRepresentative)
        {
            super(packageRepresentative);
        }

        @Override
        protected void fillValidatorTypes(
                final EnumMap<Validation, Class<? extends TagValidator>> validatorTypes)
        {
            super.fillValidatorTypes(validatorTypes);
            validatorTypes.put(Validation.DOUBLE, IllegalValidator.class);
        }
    }

    /**
     * Validators implementation that removes the DOUBLE validator so we can test the missing
     * validator code path within Validators
     *
     * @author cstaylor
     */
    private static final class TestingNullValidators extends Validators
    {
        TestingNullValidators(final Class<?> packageRepresentative)
        {
            super(packageRepresentative);
        }

        @Override
        protected void fillValidatorTypes(
                final EnumMap<Validation, Class<? extends TagValidator>> validatorTypes)
        {
            super.fillValidatorTypes(validatorTypes);
            validatorTypes.remove(Validation.DOUBLE);
        }
    }

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Test
    public void illegalValidator()
    {
        this.expected.expect(IllegalArgumentException.class);
        new TestingIllegalValidators(ValidatorsTestCase.class);
    }

    @Test
    public void nullValidator()
    {
        this.expected.expect(IllegalArgumentException.class);
        new TestingNullValidators(ValidatorsTestCase.class);
    }
}
