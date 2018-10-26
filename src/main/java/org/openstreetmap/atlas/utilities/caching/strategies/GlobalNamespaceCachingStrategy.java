package org.openstreetmap.atlas.utilities.caching.strategies;

/**
 * A special case of {@link NamespaceCachingStrategy} that uses a predefined, global namespace. This
 * means that all instances of {@link GlobalNamespaceCachingStrategy} will share the same underlying
 * contents. From this it follows that fetches and invalidates will manifest across instances.
 *
 * @author lcram
 */
public class GlobalNamespaceCachingStrategy extends NamespaceCachingStrategy
{
    /*
     * This is a random SHA256 hash. Collisions with this namespace are astronomically unlikely (due
     * to the 256 bits of entropy).
     */
    private static final String GLOBAL_NAMESPACE = "3707740A818531237051A0F1E086CF701E2C38483675FCD1AAD8F5C5C33F19BC";

    public GlobalNamespaceCachingStrategy()
    {
        super(GLOBAL_NAMESPACE);
    }

    @Override
    public String getName()
    {
        return "GlobalNamespaceCachingStrategy";
    }
}
