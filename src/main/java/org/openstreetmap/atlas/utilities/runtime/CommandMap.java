package org.openstreetmap.atlas.utilities.runtime;

import java.util.HashMap;
import java.util.Optional;

import org.openstreetmap.atlas.utilities.runtime.Command.Switch;

/**
 * Get directly from a Switch.
 *
 * @author matthieun
 */
public class CommandMap extends HashMap<String, Object>
{
    private static final long serialVersionUID = 670017778010945895L;

    public Object get(final Switch<?> aSwitch)
    {
        return get(aSwitch.getName());
    }

    public Optional<?> getOption(final Switch<?> aSwitch)
    {
        final Object object = get(aSwitch);
        return Optional.ofNullable(object);
    }
}
