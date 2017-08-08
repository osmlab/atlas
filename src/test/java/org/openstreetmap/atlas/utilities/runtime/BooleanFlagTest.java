package org.openstreetmap.atlas.utilities.runtime;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * JUnit test case for testing the following situations:
 * <ul>
 * <li>testBooleanOptionalFlagFound - flag is optional and specified by the user (TRUE)</li>
 * <li>testBooleanOptionalFlagMissing - flag is optional and not specified by the user (FALSE)</li>
 * <li>testBooleanRequiredFlagFound - flag is required and specified by the user (TRUE)</li>
 * <li>testBooleanRequiredFlagFound - flag is required and not specified by the user (COREEXCEPTION)
 * </li>
 * </ul>
 *
 * @author cstaylor
 */
public class BooleanFlagTest
{
    /**
     * Command object for verifying the optional flag tests
     *
     * @author cstaylor
     */
    static final class OptionalBooleanFlags extends Command
    {
        private static final Flag VERBOSE_FLAG = new Flag("v", "The Verbose Flag");

        private boolean success;

        public boolean isSuccessful()
        {
            return this.success;
        }

        @Override
        protected int onRun(final CommandMap command)
        {
            this.success = (Boolean) command.get(VERBOSE_FLAG);
            return 0;
        }

        @Override
        protected SwitchList switches()
        {
            return new SwitchList().with(VERBOSE_FLAG);
        }
    }

    /**
     * Command object for verifying the required flag tests
     *
     * @author cstaylor
     */

    static final class RequiredBooleanFlags extends Command
    {
        private static final Flag VERBOSE_FLAG = new Flag("v", "The Verbose Flag",
                Optionality.REQUIRED);

        private boolean success;

        public boolean isSuccessful()
        {
            return this.success;
        }

        @Override
        protected int onRun(final CommandMap command)
        {
            this.success = (Boolean) command.get(VERBOSE_FLAG);
            return 0;
        }

        @Override
        protected SwitchList switches()
        {
            return new SwitchList().with(VERBOSE_FLAG);
        }
    }

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testBooleanOptionalFlagFound()
    {
        final OptionalBooleanFlags flags = new OptionalBooleanFlags();
        flags.runWithoutQuitting("-v");
        Assert.assertTrue(flags.isSuccessful());
    }

    @Test
    public void testBooleanOptionalFlagMissing()
    {
        final OptionalBooleanFlags flags = new OptionalBooleanFlags();
        flags.runWithoutQuitting("");
        Assert.assertFalse(flags.isSuccessful());
    }

    @Test
    public void testBooleanRequiredFlagFound()
    {
        final RequiredBooleanFlags flags = new RequiredBooleanFlags();
        flags.runWithoutQuitting("-v");
        Assert.assertTrue(flags.isSuccessful());
    }

    @Test
    public void testBooleanRequiredFlagMissing()
    {
        this.expected.expect(CoreException.class);
        final RequiredBooleanFlags flags = new RequiredBooleanFlags();
        flags.runWithoutQuitting("");
        Assert.fail("Shouldn't get here");
    }
}
