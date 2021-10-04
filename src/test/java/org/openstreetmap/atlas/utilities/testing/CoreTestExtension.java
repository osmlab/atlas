package org.openstreetmap.atlas.utilities.testing;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit 4 to JUnit 5 shim for {@link CoreTestRule}. It is still compatible with JUnit 4.
 *
 * @author Taylor Smock
 */
public class CoreTestExtension extends CoreTestRule implements BeforeEachCallback
{
    @Override
    public void beforeEach(final ExtensionContext context) throws Exception
    {
        final Statement temporaryStatement = new EmptyStatement();
        try
        {
            this.apply(temporaryStatement, Description.createTestDescription(this.getClass(),
                    "CoreTestRule JUnit5 Compatibility")).evaluate();
        }
        catch (final Throwable exception)
        {
            throw new Exception(exception);
        }
    }

    /**
     * This exists solely to provide a base statement for JUnit 4 -> 5 transitioning
     */
    private static class EmptyStatement extends Statement
    {
        @Override
        public void evaluate() throws Throwable
        {
            // do nothing
        }
    }
}
