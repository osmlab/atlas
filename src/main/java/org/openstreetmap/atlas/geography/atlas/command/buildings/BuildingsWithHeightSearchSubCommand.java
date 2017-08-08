package org.openstreetmap.atlas.geography.atlas.command.buildings;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.apache.commons.compress.utils.IOUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.command.AbstractAtlasSubCommand;
import org.openstreetmap.atlas.geography.atlas.command.AtlasCommandConstants;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuilding;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

import com.google.common.collect.ComparisonChain;

/**
 * Outputs information about buildings that have heights (making them 3D)
 *
 * @author cstaylor
 */
public class BuildingsWithHeightSearchSubCommand extends AbstractAtlasSubCommand
{
    /**
     * Data item class for holding onto the information we need to log about building heights
     *
     * @author cstaylor
     */
    private static final class BuildingHeightItem implements Comparable<BuildingHeightItem>
    {
        private final String iso3;
        private double height;
        private final long osmIdentifier;
        private final long atlasIdentifier;
        private final String latitude;
        private final String longitude;
        private final String url;
        private final boolean valid;

        BuildingHeightItem(final ComplexBuilding building)
        {
            this.url = String.format("http://www.openstreetmap.org/%s/%d",
                    building.getSource().getType() == ItemType.AREA ? "way" : "relation",
                    building.getOsmIdentifier());
            final MultiPolygon outline = building.getOutline();
            this.valid = outline != null;
            if (this.valid)
            {
                final Location location = building.getOutline().outers().iterator().next().first();
                this.iso3 = building.getTag(ISOCountryTag.class, Optional.empty()).orElse("UNK");
                this.atlasIdentifier = building.getIdentifier();
                this.osmIdentifier = building.getOsmIdentifier();
                building.topHeight().ifPresent(height ->
                {
                    this.height = height.asMeters();
                });
                this.latitude = location.getLatitude().toString();
                this.longitude = location.getLongitude().toString();
            }
            else
            {
                this.iso3 = null;
                this.atlasIdentifier = -1;
                this.osmIdentifier = -1;
                this.latitude = null;
                this.longitude = null;
            }
        }

        @Override
        public int compareTo(final BuildingHeightItem otherBuilding)
        {
            return ComparisonChain.start().compare(this.iso3, otherBuilding.iso3)
                    .compare(this.height, otherBuilding.height)
                    .compare(this.atlasIdentifier, otherBuilding.atlasIdentifier).result();
        }

        @Override
        public boolean equals(final Object otherObject)
        {
            if (this == otherObject)
            {
                return true;
            }
            if (otherObject instanceof BuildingHeightItem)
            {
                final BuildingHeightItem otherItem = (BuildingHeightItem) otherObject;
                return this.atlasIdentifier == otherItem.atlasIdentifier;
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.atlasIdentifier);
        }

        private boolean isValid()
        {
            return this.valid;
        }

        private void output(final int count, final PrintStream stream)
        {
            stream.printf(
                    "<tr><td>%d</td><td>%s</td><td align=\"right\">%s</td><td align=\"right\">%s</td><td><a href=\"%s\">%s</td><td>%.2f</td><td>%s,%s</td></tr>\n",
                    count, this.iso3, this.atlasIdentifier, this.osmIdentifier, this.url, this.url,
                    this.height, this.latitude, this.longitude);
        }
    }

    /**
     * Logs some basic information about each building that has a height
     *
     * @author cstaylor
     */
    private static final class BuildingsWithHeightLogger
            implements Consumer<ComplexBuilding>, Closeable
    {
        private final PrintStream output;
        private final TreeSet<BuildingHeightItem> items;

        private BuildingsWithHeightLogger(final PrintStream output)
        {
            this.output = output;
            output.printf(
                    "<html><head><style>table { font-family: Menlo; }</style></head><body><table>\n");
            this.items = new TreeSet<>();
        }

        @Override
        public void accept(final ComplexBuilding buildingsWithHeight)
        {
            final BuildingHeightItem item = new BuildingHeightItem(buildingsWithHeight);
            if (item.isValid())
            {
                this.items.add(item);
            }
        }

        @Override
        public void close() throws IOException
        {
            final int[] counter = { 0 };
            this.items.stream().forEach(item ->
            {
                item.output(++counter[0], this.output);
            });
            this.output.printf("</table></body></html>\n");
            IOUtils.closeQuietly(this.output);
        }
    }

    private static final Switch<Path> OUTPUT_FILE_PARAMETER = new Switch<>("output",
            "HTML file containing information about each 3D building", Paths::get,
            Optionality.REQUIRED);

    private BuildingsWithHeightLogger counter;

    public BuildingsWithHeightSearchSubCommand()
    {
        super("3d-buildings", "Lists all of the buildings that have a height value");
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
        writer.printf("-output=/path/to/output/file\n");
    }

    @Override
    protected int finish(final CommandMap command)
    {
        try
        {
            this.counter.close();
        }
        catch (final IOException oops)
        {
            throw new CoreException("Failure to close", oops);
        }
        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        StreamSupport.stream(new ComplexBuildingFinder().find(atlas).spliterator(), false)
                .map(ComplexBuilding.class::cast).filter(this::hasHeight).forEach(this.counter);
    }

    @Override
    protected void start(final CommandMap command)
    {
        super.start(command);
        this.counter = new BuildingsWithHeightLogger(createStream(command));
    }

    private PrintStream createStream(final CommandMap command)
    {
        try
        {
            final Path output = (Path) command.get(OUTPUT_FILE_PARAMETER);
            try
            {
                Files.createDirectories(output.getParent());
            }
            catch (final IOException oops)
            {
                throw new CoreException("Error when creating output directory", oops);
            }
            return new PrintStream(new BufferedOutputStream(new FileOutputStream(output.toFile())));
        }
        catch (final IOException oops)
        {
            throw new CoreException("Failure to open output", oops);
        }
    }

    private boolean hasHeight(final ComplexBuilding building)
    {
        return building.topHeight().isPresent();
    }
}
