package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that outputs the atlas file and OSM id of any areas missing ISO country codes from the
 * supplied atlas
 *
 * @author cstaylor
 */
public class AtlasMissingISOSubCommand extends AbstractAtlasSubCommand
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasMissingISOSubCommand.class);

    private AtomicInteger counter;
    private Time start;

    public AtlasMissingISOSubCommand()
    {
        super("isoloss", "outputs all of the atlas objects that are missing ISO country codes");
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(
                "-combine : merge all of the atlas files into a MultiAtlas before outputting geojson\n");
    }

    @Override
    protected int finish(final CommandMap command)
    {
        final int value = this.counter.get();
        if (value > 0)
        {
            logger.error(String.format("Total Items missing ISO Codes: %s",
                    DecimalFormat.getNumberInstance().format(value)));
        }
        logger.info("Time elapsed {}", this.start.elapsedSince());
        return Math.min(value, 1);
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        final Predicate<Taggable> missingISOCountryTag = Validators
                .hasValuesFor(ISOCountryTag.class).negate();
        StreamSupport.stream(atlas.items().spliterator(), true).filter(missingISOCountryTag)
                .forEach(this::log);
    }

    @Override
    protected void start(final CommandMap command)
    {
        this.counter = new AtomicInteger();
        this.start = Time.now();
    }

    private void log(final AtlasItem item)
    {
        this.counter.incrementAndGet();
        logger.error(String.format("[item] [%25s] [%9d]", item.getAtlas().getName(),
                item.getOsmIdentifier()));
    }
}
