package org.openstreetmap.atlas.geography.atlas.pbf;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.filters.ConfiguredTaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * With this {@link AtlasLoadingOption} you can specify which feature you want to load to Atlas
 *
 * @author tony
 */
public final class AtlasLoadingOption implements Serializable
{
    private static final long serialVersionUID = 1811691207451027561L;

    public static final String ATLAS_EDGE_FILTER_NAME = "atlas-edge";
    public static final String ATLAS_AREA_FILTER_NAME = "atlas-area";
    public static final String ATLAS_RELATION_SLICING_FILTER_NAME = "atlas-relation-slicing";
    public static final String ATLAS_RELATION_SLICING_CONSOLIDATE_FILTER_NAME = "atlas-relation-slicing-consolidate";
    public static final String ATLAS_WAY_SECTION_FILTER_NAME = "atlas-way-section";

    private static final BridgeConfiguredFilter DEFAULT_EDGE_FILTER = new BridgeConfiguredFilter("",
            ATLAS_EDGE_FILTER_NAME,
            new StandardConfiguration(new InputStreamResource(() -> AtlasLoadingOption.class
                    .getResourceAsStream(ATLAS_EDGE_FILTER_NAME + FileSuffix.JSON.toString()))));
    private static final BridgeConfiguredFilter DEFAULT_AREA_FILTER = new BridgeConfiguredFilter("",
            ATLAS_AREA_FILTER_NAME,
            new StandardConfiguration(new InputStreamResource(() -> AtlasLoadingOption.class
                    .getResourceAsStream(ATLAS_AREA_FILTER_NAME + FileSuffix.JSON.toString()))));

    private static final BridgeConfiguredFilter DEFAULT_RELATION_SLICING_FILTER = new BridgeConfiguredFilter(
            "", ATLAS_RELATION_SLICING_FILTER_NAME,
            new StandardConfiguration(
                    new InputStreamResource(() -> AtlasLoadingOption.class.getResourceAsStream(
                            ATLAS_RELATION_SLICING_FILTER_NAME + FileSuffix.JSON.toString()))));

    private static final BridgeConfiguredFilter DEFAULT_RELATION_SLICING_CONSOLIDATE_FILTER = new BridgeConfiguredFilter(
            "", ATLAS_RELATION_SLICING_CONSOLIDATE_FILTER_NAME,
            new StandardConfiguration(
                    new InputStreamResource(() -> AtlasLoadingOption.class.getResourceAsStream(
                            ATLAS_RELATION_SLICING_FILTER_NAME + FileSuffix.JSON.toString()))));

    private static final BridgeConfiguredFilter DEFAULT_WAY_SECTION_FILTER = new BridgeConfiguredFilter(
            "", ATLAS_WAY_SECTION_FILTER_NAME,
            new StandardConfiguration(
                    new InputStreamResource(() -> AtlasLoadingOption.class.getResourceAsStream(
                            ATLAS_WAY_SECTION_FILTER_NAME + FileSuffix.JSON.toString()))));
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
    private BridgeConfiguredFilter edgeFilter = DEFAULT_EDGE_FILTER;
    private BridgeConfiguredFilter areaFilter = DEFAULT_AREA_FILTER;
    private BridgeConfiguredFilter waySectionFilter = DEFAULT_WAY_SECTION_FILTER;
    private ConfiguredTaggableFilter osmPbfWayFilter = DEFAULT_OSM_PBF_WAY_FILTER;
    private ConfiguredTaggableFilter osmPbfNodeFilter = DEFAULT_OSM_PBF_NODE_FILTER;
    private ConfiguredTaggableFilter osmPbfRelationFilter = DEFAULT_OSM_PBF_RELATION_FILTER;
    private BridgeConfiguredFilter relationSlicingFilter = DEFAULT_RELATION_SLICING_FILTER;
    private BridgeConfiguredFilter relationSlicingConsolidateFilter = DEFAULT_RELATION_SLICING_CONSOLIDATE_FILTER;

    private boolean loadAtlasRelation;
    private boolean loadOsmBound;
    private boolean countrySlicing;
    /** Used to indicate that all objects should be kept */
    private boolean keepAll;
    private boolean waySectioning;
    private boolean loadWaysSpanningCountryBoundaries;
    private String countryCode;

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

    public static AtlasLoadingOption createOptionWithOnlySectioning()
    {
        final AtlasLoadingOption option = new AtlasLoadingOption();
        option.setCountrySlicing(false);
        option.setWaySectioning(true);
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
        this.keepAll = false;
        this.waySectioning = false;
        this.loadWaysSpanningCountryBoundaries = true;
        this.countryBoundaryMap = null;
    }

    public BridgeConfiguredFilter getAreaFilter()
    {
        return this.areaFilter;
    }

    public CountryBoundaryMap getCountryBoundaryMap()
    {
        return this.countryBoundaryMap;
    }

    public String getCountryCode()
    {
        return this.countryCode;
    }

    public BridgeConfiguredFilter getEdgeFilter()
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

    public BridgeConfiguredFilter getRelationSlicingConsolidateFilter()
    {
        return this.relationSlicingConsolidateFilter;
    }

    public BridgeConfiguredFilter getRelationSlicingFilter()
    {
        return this.relationSlicingFilter;
    }

    public BridgeConfiguredFilter getWaySectionFilter()
    {
        return this.waySectionFilter;
    }

    public boolean isCountrySlicing()
    {
        return this.countrySlicing;
    }

    /**
     * Check to see if the atlas should not be filtered or deduplicated. This option takes
     * precedence over all filtering options.
     *
     * @return {@code true} if we should not drop any items
     */
    public boolean isKeepAll()
    {
        return this.keepAll;
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

    public void setAreaFilter(final BridgeConfiguredFilter areaFilter)
    {
        this.areaFilter = areaFilter;
    }

    public void setCountryBoundaryMap(final CountryBoundaryMap countryBoundaryMap)
    {
        this.countryBoundaryMap = countryBoundaryMap;
    }

    public AtlasLoadingOption setCountryCode(final String countryCode)
    {
        this.countryCode = countryCode;
        return this;
    }

    public AtlasLoadingOption setCountrySlicing(final boolean isCountrySlicing)
    {
        this.countrySlicing = isCountrySlicing;
        return this;
    }

    public void setEdgeFilter(final BridgeConfiguredFilter edgeFilter)
    {
        this.edgeFilter = edgeFilter;
    }

    /**
     * Set whether or not all objects should be kept, regardless of filters.
     *
     * @param isKeepAll
     *            {@code true} to keep all objects
     * @return {@code this}, for easy chaining
     */
    public AtlasLoadingOption setKeepAll(final boolean isKeepAll)
    {
        this.keepAll = isKeepAll;
        return this;
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

    public void setRelationSlicingConsolidateFilter(
            final BridgeConfiguredFilter relationSlicingConsolidateFilter)
    {
        this.relationSlicingConsolidateFilter = relationSlicingConsolidateFilter;
    }

    public void setRelationSlicingFilter(final BridgeConfiguredFilter relationSlicingFilter)
    {
        this.relationSlicingFilter = relationSlicingFilter;
    }

    public void setWaySectionFilter(final BridgeConfiguredFilter waySectionFilter)
    {
        this.waySectionFilter = waySectionFilter;
    }

    public AtlasLoadingOption setWaySectioning(final boolean isWaySectioning)
    {
        this.waySectioning = isWaySectioning;
        return this;
    }
}
