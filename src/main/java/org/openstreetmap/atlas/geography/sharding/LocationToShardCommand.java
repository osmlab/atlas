package org.openstreetmap.atlas.geography.sharding;

import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * @author matthieun
 */
public class LocationToShardCommand extends Command
{
    private static final Switch<Sharding> SHARDING = new Switch<>("sharding", "The sharding tree",
            Sharding::forString, Optionality.REQUIRED);
    private static final Switch<Location> LOCATION = new Switch<>("location",
            "The location to check as \"latitude,longitude\"", Location::forString,
            Optionality.OPTIONAL);
    private static final Switch<Location> WKT_POINT = new Switch<>("wktPoint",
            "The location to check as a WKT point", Location::forWkt, Optionality.OPTIONAL);

    public static void main(final String[] args)
    {
        new LocationToShardCommand().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Sharding sharding = (Sharding) command.get(SHARDING);
        Location location = (Location) command.get(LOCATION);
        if (location == null)
        {
            location = (Location) command.get(WKT_POINT);
        }
        if (location == null)
        {
            System.err.println("No location found! Make sure to use either the -"
                    + LOCATION.getName() + " or -" + WKT_POINT.getName() + " switch.");
            return 0;
        }
        final Set<? extends Shard> shards = Iterables.asSet(sharding.shardsCovering(location));
        if (shards.size() > 0)
        {
            System.out.println("Shard(s) covering " + location.toCompactString() + ":");
            shards.forEach(
                    shard -> System.out.println(shard.getName() + "; " + shard.bounds().toWkt()));
        }
        else
        {
            System.err.println("No shard found!");
            return 1;
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(SHARDING, LOCATION, WKT_POINT);
    }
}
