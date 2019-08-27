package org.openstreetmap.atlas.geography.atlas.command;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Checks atlas items in a given atlas for geometry with consecutive identical shapepoints. While
 * this is acceptable in OSM data, it could cause problems in downstream processing tools, so this
 * tool can give us an early warning.
 * <p>
 * Two required parameters:
 * <ul>
 * <li>-input: where we should read the atlas files</li>
 * <li>-output: where we should write information about consecutive identical shapepoints. These
 * files are organized by country</li>
 * </ul>
 *
 * @author cstaylor
 */
public class AtlasItemsWithSharedShapepointsSubCommand extends AbstractAtlasSubCommand
{
    /**
     * Helper class for capturing the geometry of an item as a PolyLine and deciding if it has any
     * consecutive identical shapepoints
     *
     * @author cstaylor
     */
    private static final class PolyLineTrouble
    {
        private final long osmId;
        private final PolyLine polyline;

        PolyLineTrouble(final AtlasItem item)
        {
            this.osmId = item.getOsmIdentifier();
            this.polyline = new PolyLine(item.getRawGeometry());
        }

        Long getOsmId()
        {
            return this.osmId;
        }

        /**
         * Simple algorithm for checking if the polyline has consecutive identical shapepoints by
         * comparing the current point with the previous one
         *
         * @return true if there is a least one overlapping point, false otherwise
         */
        boolean hasDuplicatePoints()
        {
            Location previous = null;
            for (final Location location : this.polyline)
            {
                if (location.equals(previous))
                {
                    return true;
                }
                previous = location;
            }
            return false;
        }
    }

    private static final Logger logger = LoggerFactory
            .getLogger(AtlasItemsWithSharedShapepointsSubCommand.class);

    private static final Switch<Path> OUTPUT_FILE_PARAMETER = new Switch<>("output",
            "Where we want to store the duplicate point error logs", Paths::get,
            Optionality.REQUIRED);

    private Multimap<String, Long> countryToOSMids;

    private Time start;

    private int atlasFiles;

    public AtlasItemsWithSharedShapepointsSubCommand()
    {
        super("duplicate-points",
                "Outputs all of the OSM ids for items with consecutive identical shapepoints");
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(OUTPUT_FILE_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf("\t-output=/path/to/output/folder/for/each/country%n");
    }

    @Override
    protected int finish(final CommandMap command)
    {
        for (final Entry<String, Collection<Long>> badIdsForCountry : this.countryToOSMids.asMap()
                .entrySet())
        {
            try (PrintStream stream = outputFor(badIdsForCountry.getKey(), command))
            {
                stream.println(badIdsForCountry.getValue().stream().sorted().map(String::valueOf)
                        .collect(Collectors.joining("\n")));
            }
        }
        final long totalErrors = this.countryToOSMids.asMap().values().parallelStream()
                .flatMap(Collection::parallelStream).count();
        final NumberFormat formatter = DecimalFormat.getIntegerInstance();
        if (logger.isInfoEnabled())
        {
            logger.info("Completed: {} atlas files and found {} errors from {} countries took {}",
                    formatter.format(this.atlasFiles), formatter.format(totalErrors),
                    formatter.format(this.countryToOSMids.keySet().size()),
                    this.start.elapsedSince());
        }
        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        logger.info("Starting {}", atlas.getName());
        this.atlasFiles++;
        final Set<Long> badOsmIDS = ConcurrentHashMap.newKeySet();
        StreamSupport.stream(atlas.items().spliterator(), true).map(PolyLineTrouble::new)
                .filter(PolyLineTrouble::hasDuplicatePoints).map(PolyLineTrouble::getOsmId)
                .forEach(badOsmIDS::add);
        final Optional<String> countryNameOption = atlas.metaData().getCountry();
        if (countryNameOption.isPresent())
        {
            final String countryName = countryNameOption.get();
            badOsmIDS.forEach(id ->
            {
                this.countryToOSMids.put(countryName, id);
            });
            if (!badOsmIDS.isEmpty())
            {
                logger.warn("Found {} overlaps in {}", badOsmIDS.size(), atlas.getName());
            }
        }
    }

    protected PrintStream outputFor(final String isoCountry, final CommandMap command)
    {
        final Path outputDirectory = (Path) command.get(OUTPUT_FILE_PARAMETER);
        try
        {
            Files.createDirectories(outputDirectory);
            final Path outputFile = outputDirectory
                    .resolve(String.format("%s.duplicatepoints", isoCountry));
            return new PrintStream(
                    new BufferedOutputStream(new FileOutputStream(outputFile.toFile())));
        }
        catch (final IOException oops)
        {
            throw new CoreException("Failure when creating outputstream for {}", isoCountry, oops);
        }
    }

    @Override
    protected void start(final CommandMap command)
    {
        super.start(command);
        this.countryToOSMids = ArrayListMultimap.create();
        this.start = Time.now();
    }
}
