package org.openstreetmap.atlas.utilities.runtime;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;

/**
 * This test case verifies the switch name parsing bug CST introduced on 2015-12-11.
 *
 * @author cstaylor
 */
public class FlagNameParsingRegressionTest
{
    /**
     * Sample command implementation for verifying unknown switches and their names
     *
     * @author cstaylor
     */
    static final class VerifySwitchNames extends Command
    {
        private boolean success;

        public boolean isSuccessful()
        {
            return this.success;
        }

        @Override
        protected int onRun(final CommandMap command)
        {
            this.success = command.containsKey("theunknownswitch");
            this.success = this.success && "yup".equals(command.get(KNOWN_SWITCH));
            return 0;
        }

        @Override
        protected SwitchList switches()
        {
            return new SwitchList().with(KNOWN_SWITCH);
        }
    }

    private static final Switch<String> KNOWN_SWITCH = new Switch<>("theknownswitch",
            "We should find this switch", path -> path, Optionality.OPTIONAL);

    @Test
    public void testUnknownFlag()
    {
        final VerifySwitchNames flags = new VerifySwitchNames();
        flags.runWithoutQuitting("-theunknownswitch=nope", "-theknownswitch=yup");
        Assert.assertTrue(flags.isSuccessful());
    }
}
