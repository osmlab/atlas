package org.openstreetmap.atlas.geography.atlas.pbf;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.filters.ConfiguredTaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * With this {@link AtlasLoadingOption} you can specify which feature you want to load to Atlas
 *
 * @author tony
 */
public final class AtlasLoadingOption implements Serializable
{
    private static final long serialVersionUID = 1811691207451027561L;

    private static final ConfiguredTaggableFilter DEFAULT_EDGE_FILTER = new ConfiguredTaggableFilter(
            new StandardConfiguration(new InputStreamResource(
                    () -> AtlasLoadingOption.class.getResourceAsStream("atlas-edge.json"))));
    private static final ConfiguredTaggableFilter DEFAULT_WAY_SECTION_FILTER = new ConfiguredTaggableFilter(
            new StandardConfiguration(new InputStreamResource(
                    () -> AtlasLoadingOption.class.getResourceAsStream("atlas-way-section.json"))));
    private static final ConfiguredTaggableFilter DEFAULT_OSM_PBF_WAY_FILTER = new ConfiguredTaggableFilter(
            new StandardConfiguration(new InputStreamResource(
                    () -> AtlasLoadingOption.class.getResourceAsStream("osm-pbf-way.json"))));
    private static final ConfiguredTaggableFilter DEFAULT_OSM_PBF_NODE_FILTER = new ConfiguredTaggableFilter(
            new StandardConfiguration(new InputStreamResource(
                    () -> AtlasLoadingOption.class.getResourceAsStream("osm-pbf-node.json"))));
    private static final ConfiguredTaggableFilter DEFAULT_OSM_PBF_RELATION_FILTER = new ConfiguredTaggableFilter(
            new StandardConfiguration(new InputStreamResource(
                    () -> AtlasLoadingOption.class.getResourceAsStream("osm-pbf-relation.json"))));

    private boolean loadAtlasPoint;
    private boolean loadAtlasLine;
    private boolean loadAtlasArea;
    private boolean loadAtlasNode;
    private boolean loadAtlasEdge;
    private ConfiguredTaggableFilter edgeFilter = DEFAULT_EDGE_FILTER;
    private ConfiguredTaggableFilter waySectionFilter = DEFAULT_WAY_SECTION_FILTER;
    private ConfiguredTaggableFilter osmPbfWayFilter = DEFAULT_OSM_PBF_WAY_FILTER;
    private ConfiguredTaggableFilter osmPbfNodeFilter = DEFAULT_OSM_PBF_NODE_FILTER;
    private ConfiguredTaggableFilter osmPbfRelationFilter = DEFAULT_OSM_PBF_RELATION_FILTER;
    private boolean loadAtlasRelation;
    private boolean loadOsmBound;
    private boolean countrySlicing;
    private boolean waySectioning;
    private boolean loadWaysSpanningCountryBoundaries;
    private final Set<String> countryCodes = new HashSet<>();

    private CountryBoundaryMap countryBoundaryMap;

    public static AtlasLoadingOption createOptionWithAllEnabled(
            final CountryBoundaryMap countryBoundaryMap)
    {
        final AtlasLoadingOption option = new AtlasLoadingOption();
        option.setCountrySlicing(true);
        option.setWaySectioning(true);
        option.setCountryBoundaryMap(countryBoundaryMap);
        return option;
    }

    public static AtlasLoadingOption createOptionWithNoSlicing()
    {
        return new AtlasLoadingOption();
    }

    public static AtlasLoadingOption createOptionWithOnlyNodesAndWayNoSlicing()
    {
        final AtlasLoadingOption option = new AtlasLoadingOption();
        option.setLoadAtlasPoint(false);
        option.setLoadAtlasLine(false);
        option.setLoadAtlasArea(false);
        option.setLoadAtlasRelation(false);
        return option;
    }

    public static AtlasLoadingOption createOptionWithOnlyNodesAndWaysAndSlicing(
            final CountryBoundaryMap countryBoundaryMap)
    {
        final AtlasLoadingOption option = new AtlasLoadingOption();
        option.setLoadAtlasPoint(false);
        option.setLoadAtlasLine(false);
        option.setLoadAtlasArea(false);
        option.setLoadAtlasRelation(false);
        option.setCountrySlicing(true);
        option.setWaySectioning(true);
        option.setCountryBoundaryMap(countryBoundaryMap);
        return option;
    }

    public static AtlasLoadingOption withNoFilter()
    {
        final StringResource pbfFilter = new StringResource("{\"filters\":[]}");
        final ConfiguredTaggableFilter filter = new ConfiguredTaggableFilter(
                new StandardConfiguration(pbfFilter));
        final AtlasLoadingOption atlasLoadingOption = new AtlasLoadingOption();
        atlasLoadingOption.setOsmPbfWayFilter(filter);
        atlasLoadingOption.setOsmPbfNodeFilter(filter);
        atlasLoadingOption.setOsmPbfRelationFilter(filter);
        atlasLoadingOption.setWaySectioning(true);
        return atlasLoadingOption;
    }

    private AtlasLoadingOption()
    {
        this.loadAtlasPoint = true;
        this.loadAtlasNode = true;
        this.loadAtlasLine = true;
        this.loadAtlasEdge = true;
        this.loadAtlasArea = true;
        this.loadAtlasRelation = true;
        this.loadOsmBound = true;
        this.countrySlicing = false;
        this.waySectioning = false;
        this.loadWaysSpanningCountryBoundaries = true;
        this.countryBoundaryMap = null;
    }

    public CountryBoundaryMap getCountryBoundaryMap()
    {
        return this.countryBoundaryMap;
    }

    public Set<String> getCountryCodes()
    {
        return this.countryCodes;
    }

    public ConfiguredTaggableFilter getEdgeFilter()
    {
        return this.edgeFilter;
    }

    public ConfiguredTaggableFilter getOsmPbfNodeFilter()
    {
        return this.osmPbfNodeFilter;
    }

    public ConfiguredTaggableFilter getOsmPbfRelationFilter()
    {
        return this.osmPbfRelationFilter;
    }

    public ConfiguredTaggableFilter getOsmPbfWayFilter()
    {
        return this.osmPbfWayFilter;
    }

    public ConfiguredTaggableFilter getWaySectionFilter()
    {
        return this.waySectionFilter;
    }

    public boolean isCountrySlicing()
    {
        return this.countrySlicing;
    }

    public boolean isLoadAtlasArea()
    {
        return this.loadAtlasArea;
    }

    public boolean isLoadAtlasEdge()
    {
        return this.loadAtlasEdge;
    }

    public boolean isLoadAtlasLine()
    {
        return this.loadAtlasLine;
    }

    public boolean isLoadAtlasNode()
    {
        return this.loadAtlasNode;
    }

    public boolean isLoadAtlasPoint()
    {
        return this.loadAtlasPoint;
    }

    public boolean isLoadAtlasRelation()
    {
        return this.loadAtlasRelation;
    }

    public boolean isLoadOsmBound()
    {
        return this.loadOsmBound;
    }

    public boolean isLoadOsmNode()
    {
        return isLoadAtlasNode() || isLoadAtlasPoint();
    }

    public boolean isLoadOsmRelation()
    {
        return isLoadAtlasRelation();
    }

    public boolean isLoadOsmWay()
    {
        return isLoadAtlasEdge() || isLoadAtlasLine();
    }

    public boolean isLoadWaysSpanningCountryBoundaries()
    {
        return this.loadWaysSpanningCountryBoundaries;
    }

    public boolean isWaySectioning()
    {
        return this.waySectioning;
    }

    public AtlasLoadingOption setAdditionalCountryCodes(final Iterable<String> countryCodes)
    {
        this.countryCodes.addAll(Iterables.asSet(countryCodes));
        return this;
    }

    public AtlasLoadingOption setAdditionalCountryCodes(final String... countryCodes)
    {
        return setAdditionalCountryCodes(Iterables.asSet(countryCodes));
    }

    public void setCountryBoundaryMap(final CountryBoundaryMap countryBoundaryMap)
    {
        this.countryBoundaryMap = countryBoundaryMap;
    }

    public AtlasLoadingOption setCountrySlicing(final boolean isCountrySlicing)
    {
        this.countrySlicing = isCountrySlicing;
        return this;
    }

    public void setEdgeFilter(final ConfiguredTaggableFilter edgeFilter)
    {
        this.edgeFilter = edgeFilter;
    }

    public AtlasLoadingOption setLoadAtlasArea(final boolean isLoadAtlasArea)
    {
        this.loadAtlasArea = isLoadAtlasArea;
        return this;
    }

    public AtlasLoadingOption setLoadAtlasEdge(final boolean isLoadAtlasEdge)
    {
        this.loadAtlasEdge = isLoadAtlasEdge;
        return this;
    }

    public AtlasLoadingOption setLoadAtlasLine(final boolean isLoadAtlasLine)
    {
        this.loadAtlasLine = isLoadAtlasLine;
        return this;
    }

    public AtlasLoadingOption setLoadAtlasNode(final boolean isLoadAtlasNode)
    {
        this.loadAtlasNode = isLoadAtlasNode;
        return this;
    }

    public AtlasLoadingOption setLoadAtlasPoint(final boolean isLoadAtlasPoint)
    {
        this.loadAtlasPoint = isLoadAtlasPoint;
        return this;
    }

    public AtlasLoadingOption setLoadAtlasRelation(final boolean isLoadAtlasRelation)
    {
        this.loadAtlasRelation = isLoadAtlasRelation;
        return this;
    }

    public AtlasLoadingOption setLoadOsmBound(final boolean isLoadOsmBound)
    {
        this.loadOsmBound = isLoadOsmBound;
        return this;
    }

    public AtlasLoadingOption setLoadWaysSpanningCountryBoundaries(
            final boolean loadWaysSpanningCountryBoundaries)
    {
        this.loadWaysSpanningCountryBoundaries = loadWaysSpanningCountryBoundaries;
        return this;
    }

    public void setOsmPbfNodeFilter(final ConfiguredTaggableFilter osmPbfNodeFilter)
    {
        this.osmPbfNodeFilter = osmPbfNodeFilter;
    }

    public void setOsmPbfRelationFilter(final ConfiguredTaggableFilter osmPbfRelationFilter)
    {
        this.osmPbfRelationFilter = osmPbfRelationFilter;
    }

    public void setOsmPbfWayFilter(final ConfiguredTaggableFilter osmPbfWayFilter)
    {
        this.osmPbfWayFilter = osmPbfWayFilter;
    }

    public void setWaySectionFilter(final ConfiguredTaggableFilter waySectionFilter)
    {
        this.waySectionFilter = waySectionFilter;
    }

    public AtlasLoadingOption setWaySectioning(final boolean isWaySectioning)
    {
        this.waySectioning = isWaySectioning;
        return this;
    }
}
