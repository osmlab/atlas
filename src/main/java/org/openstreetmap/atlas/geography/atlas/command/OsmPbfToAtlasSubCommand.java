package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.tags.filters.ConfiguredTaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

/**
 * This command converts an OSM PBF file to an Atlas file. It requires the path to a pbf and an
 * output file. It also takes a number of optional parameters.
 *
 * @author bbreithaupt
 */
public class OsmPbfToAtlasSubCommand implements FlexibleSubCommand
{
    private static final String NAME = "pbf-to-atlas";
    private static final String DESCRIPTION = "Converts a PBF to an Atlas file.";

    // Required parameters
    private static final Switch<File> INPUT_PARAMETER = new Switch<>("pbf", "Input PBF path",
            File::new, Optionality.REQUIRED);
    private static final Switch<File> OUTPUT_PARAMETER = new Switch<>("output",
            "Output Atlas file path", File::new, Optionality.REQUIRED);

    // Filter parameters
    private static final Switch<File> EDGE_FILTER_PARAMETER = new Switch<>("edge-filter",
            "Path to a json filter for determining Edges", File::new, Optionality.OPTIONAL);
    private static final Switch<File> NODE_FILTER_PARAMETER = new Switch<>("node-filter",
            "Path to a json filter for OSM nodes", File::new, Optionality.OPTIONAL);
    private static final Switch<File> RELATION_FILTER_PARAMETER = new Switch<>("relation-filter",
            "Path to a json filter for OSM relations", File::new, Optionality.OPTIONAL);
    private static final Switch<File> WAY_FILTER_PARAMETER = new Switch<>("way-filter",
            "Path to a json filter for OSM ways", File::new, Optionality.OPTIONAL);
    private static final Switch<File> WAY_SECTION_FILTER_PARAMETER = new Switch<>(
            "way-section-filter", "Path to a json filter for determining where to way section",
            File::new, Optionality.OPTIONAL);

    // Load Parameters
    private static final Switch<Boolean> LOAD_RELATIONS_PARAMETER = new Switch<>("load-relations",
            "Whether to load Relations (boolean)", Boolean::new, Optionality.OPTIONAL);
    private static final Switch<Boolean> LOAD_WAYS_PARAMETER = new Switch<>("load-ways",
            "Whether to load ways (boolean)", Boolean::new, Optionality.OPTIONAL);

    // Country parameters
    private static final Switch<Set<String>> COUNTRY_CODES_PARAMETER = new Switch<>("country-codes",
            "Countries from the country map to convert (comma separated ISO3 codes)",
            code -> Arrays.stream(code.split(",")).collect(Collectors.toSet()),
            Optionality.OPTIONAL);
    private static final Switch<File> COUNTRY_MAP_PARAMETER = new Switch<>("country-boundary-map",
            "Path to a WKT or shp file containing a country boundary map", File::new,
            Optionality.OPTIONAL);
    private static final Switch<Boolean> COUNTRY_SLICING_PARAMETER = new Switch<>("country-slicing",
            "Whether to perform country slicing (boolean)", Boolean::new, Optionality.OPTIONAL);

    // Way Sectioning Parameter
    private static final Switch<Boolean> WAY_SECTION_PARAMETER = new Switch<>("way-section",
            "Whether to perform way sectioning (boolean)", Boolean::new, Optionality.OPTIONAL);

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public SwitchList switches()
    {
        return new SwitchList().with(INPUT_PARAMETER, OUTPUT_PARAMETER, EDGE_FILTER_PARAMETER,
                NODE_FILTER_PARAMETER, RELATION_FILTER_PARAMETER, WAY_FILTER_PARAMETER,
                WAY_SECTION_FILTER_PARAMETER, LOAD_RELATIONS_PARAMETER, LOAD_WAYS_PARAMETER,
                COUNTRY_CODES_PARAMETER, COUNTRY_MAP_PARAMETER, COUNTRY_SLICING_PARAMETER,
                WAY_SECTION_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.println("-pbf=/path/to/pbf : pbf to convert");
        writer.println("-output=/path/to/output/atlas : Atlas file to output to");
        writer.println("-edge-filter=/path/to/json/edge/filter : json filter to determine Edges");
        writer.println("-node-filter=/path/to/json/node/filter : json filter for OSM nodes");
        writer.println(
                "-relation-filter=/path/to/json/relation/filter : json filter for OSM relations");
        writer.println("-way-filter=/path/to/json/way/filter : json filter for OSM ways");
        writer.println("-load-relations=boolean : whether to load Relations; defaults to true");
        writer.println("-load-ways=boolean : whether to load ways; defaults to true");
        writer.println(
                "-country-codes=list,of,ISO3,codes : countries from the country map to convert");
        writer.println(
                "-country-boundary-map=/path/to/WKT/or/shp : a WKT or shp file containing a country boundary map");
        writer.println(
                "-country-slicing=boolean : whether to perform country slicing; defaults to true");
        writer.println(
                "-way-sectioning=boolean : whether to perform way sectioning; defaults to true");
        writer.println(
                "-way-section-filter=/path/to/json/way/section/filter : json filter to determine where to way section");
    }

    @Override
    public int execute(final CommandMap map)
    {
        new OsmPbfLoader((File) map.get(INPUT_PARAMETER), this.getAtlasLoadingOption(map))
                .saveAtlas((File) map.get(OUTPUT_PARAMETER));
        return 0;
    }

    /**
     * Get or create a {@link CountryBoundaryMap}. If the country-boundary-map parameter is set,
     * this will attempt to load the text or shape file from that parameter. Else, this will load
     * using the entire world as the country UNK (unknown).
     *
     * @param map
     *            {@link CommandMap} containing the {@code COUNTRY_MAP_PARAMETER}
     * @return {@link CountryBoundaryMap} loaded from a file or default
     */
    private CountryBoundaryMap getCountryBoundaryMap(final CommandMap map)
    {
        final Optional<File> countryMapOption = (Optional<File>) map
                .getOption(COUNTRY_MAP_PARAMETER);
        CountryBoundaryMap countryMap = CountryBoundaryMap
                .fromBoundaryMap(Collections.singletonMap("UNK", MultiPolygon.MAXIMUM));
        if (countryMapOption.isPresent())
        {
            final File countryMapFile = countryMapOption.get();
            if (FilenameUtils.isExtension(countryMapFile.getName(), "txt"))
            {
                countryMap = CountryBoundaryMap.fromPlainText(countryMapFile);
            }
            else if (FilenameUtils.isExtension(countryMapFile.getName(), "shp"))
            {
                countryMap = CountryBoundaryMap.fromShapeFile(countryMapFile.getFile());
            }
        }
        return countryMap;
    }

    /**
     * Creates an {@link AtlasLoadingOption} using configurable parameters. If any of the parameters
     * are not set the defaults from {@link AtlasLoadingOption} are used.
     *
     * @param map
     *            {@link CommandMap}
     * @return {@link AtlasLoadingOption}
     */
    private AtlasLoadingOption getAtlasLoadingOption(final CommandMap map)
    {
        final CountryBoundaryMap countryMap = this.getCountryBoundaryMap(map);
        final AtlasLoadingOption options = AtlasLoadingOption
                .createOptionWithAllEnabled(countryMap);

        // Set filters
        map.getOption(EDGE_FILTER_PARAMETER).ifPresent(filter -> options.setEdgeFilter(
                new ConfiguredTaggableFilter(new StandardConfiguration((File) filter))));
        map.getOption(NODE_FILTER_PARAMETER).ifPresent(filter -> options.setOsmPbfNodeFilter(
                new ConfiguredTaggableFilter(new StandardConfiguration((File) filter))));
        map.getOption(RELATION_FILTER_PARAMETER)
                .ifPresent(filter -> options.setOsmPbfRelationFilter(
                        new ConfiguredTaggableFilter(new StandardConfiguration((File) filter))));
        map.getOption(WAY_FILTER_PARAMETER).ifPresent(filter -> options.setOsmPbfWayFilter(
                new ConfiguredTaggableFilter(new StandardConfiguration((File) filter))));
        map.getOption(WAY_SECTION_FILTER_PARAMETER).ifPresent(filter -> options.setWaySectionFilter(
                new ConfiguredTaggableFilter(new StandardConfiguration((File) filter))));

        // Set loading options
        ((Optional<Boolean>) map.getOption(LOAD_RELATIONS_PARAMETER))
                .ifPresent(options::setLoadAtlasRelation);
        ((Optional<Boolean>) map.getOption(LOAD_WAYS_PARAMETER)).ifPresent(bool ->
        {
            options.setLoadAtlasLine(bool);
            options.setLoadAtlasEdge(bool);
        });

        // Set country options
        ((Optional<Set>) map.getOption(COUNTRY_CODES_PARAMETER))
                .ifPresent(options::setAdditionalCountryCodes);
        ((Optional<Boolean>) map.getOption(COUNTRY_SLICING_PARAMETER))
                .ifPresent(options::setCountrySlicing);

        // Set way sectioning
        ((Optional<Boolean>) map.getOption(WAY_SECTION_PARAMETER))
                .ifPresent(options::setWaySectioning);

        return options;
    }
}
