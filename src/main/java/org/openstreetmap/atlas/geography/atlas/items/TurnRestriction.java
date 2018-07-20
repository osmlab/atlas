package org.openstreetmap.atlas.geography.atlas.items;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.TurnRestrictionTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSM Turn restriction, modeled from an {@link Atlas}.
 *
 * @author matthieun
 * @author sbhalekar
 */
public final class TurnRestriction implements Located, Serializable
{
    /**
     * The type of a {@link TurnRestriction}
     *
     * @author matthieun
     */
    public enum TurnRestrictionType
    {
        NO,
        ONLY,
        OTHER;
    }

    private static final Logger logger = LoggerFactory.getLogger(TurnRestriction.class);

    private static final long serialVersionUID = 7043701090121113526L;

    private Route from;
    private final Relation relation;
    private Route route;
    private Route too;
    private TurnRestrictionType type;
    private Route via;

    /**
     * Create a {@link TurnRestriction} from a {@link Relation}
     *
     * @param relation
     *            The {@link Relation} to use
     * @return An option on a {@link TurnRestriction}, which is filled if the {@link Relation} could
     *         be translated to a {@link TurnRestriction}
     */
    public static Optional<TurnRestriction> from(final Relation relation)
    {
        final TurnRestriction turnRestriction = new TurnRestriction(relation);
        if (turnRestriction.isValid())
        {
            return Optional.of(turnRestriction);
        }
        else
        {
            return Optional.empty();
        }
    }

    public static TurnRestrictionType getTurnRestrictionType(final Relation relation)
    {
        if (TurnRestrictionTag.isNoPathRestriction(relation))
        {
            return TurnRestrictionType.NO;
        }
        else if (TurnRestrictionTag.isOnlyPathRestriction(relation))
        {
            return TurnRestrictionType.ONLY;
        }
        else
        {
            return TurnRestrictionType.OTHER;
        }
    }

    /**
     * Test if a {@link Route} is restricted.
     *
     * @param candidate
     *            The {@link Route} to test
     * @return true if the {@link Route} is restricted
     */
    public static boolean isTurnRestriction(final Route candidate)
    {
        for (final Edge edge : candidate)
        {
            final Set<Relation> relations = edge.relations();
            for (final Relation relation : relations)
            {
                if (TurnRestrictionTag.isRestriction(relation))
                {
                    final Optional<TurnRestriction> turnRestrictionOption = TurnRestriction
                            .from(relation);
                    if (turnRestrictionOption.isPresent())
                    {
                        final TurnRestriction turnRestriction = turnRestrictionOption.get();
                        final Route path = turnRestriction.route();
                        switch (turnRestriction.getTurnRestrictionType())
                        {
                            case NO:
                                // There is a "No" turn restriction on one edge of the path!
                                if (path == null)
                                {
                                    continue;
                                }

                                if (candidate.isOverlapping(path)
                                        && routeContainsAllTurnRestrictionParts(turnRestriction,
                                                candidate))
                                {
                                    // All the edges in the turn restriction's path are included in
                                    // the initial BigNode route and all of the turn restriction's
                                    // parts (to/via/from) are on the path, so we can flag it as a
                                    // turn restriction. The reason for the second piece of criteria
                                    // is to avoid false positives that may overlap or contain
                                    // pieces of the turn restriction path, but not the entire
                                    // thing. For example: if there is a BigNode route that overlaps
                                    // with a turn restriction's from and via, but not the to, then
                                    // we cannot say it's a turn restriction.
                                    return true;
                                }
                                break;
                            case ONLY:
                                // There is an "Only" turn restriction on one edge of the path
                                // This is a tricky one.
                                if (path == null)
                                {
                                    continue;
                                }
                                final Route from = turnRestriction.getFrom();

                                /*
                                 * The path should be a turn restriction if all the following
                                 * criteria are met: 1. The path includes the from edge of the
                                 * turnRestriction 2. The path isn't identical to the
                                 * turnRestriction path (this is the ONLY case) 3. The path includes
                                 * an edge which is connected to the via Node 4. The edge from the
                                 * path that matches the from edge comes before the edge which is
                                 * connected to the via node (this matters for u-turn scenarios)
                                 */

                                // First make sure the from route is found within the candidate
                                // route and make sure the specified path is not a
                                // subRoute of the candidate route
                                if (candidate.isSubRoute(from) && !candidate.isSubRoute(path))
                                {
                                    final int fromSubRouteIndex = candidate.subRouteIndex(from);
                                    final int routeEndIndex = candidate.size() - 1;

                                    if (fromSubRouteIndex == routeEndIndex)
                                    {
                                        // If the from edge is the last edge in the route, the route
                                        // should not be restricted as the only directive isn't
                                        // broken, so continue searching.
                                        continue;
                                    }

                                    // Create a partial route containing everything after the from
                                    // edge(s).
                                    // fromSubRouteIndex + 1 because the subRouteIndex returns the
                                    // index of the last edge in the route. To create a partial
                                    // route of everything after the from edge, we need the next
                                    // index after the last from edge.
                                    // routeEndIndex + 1 because subRoute's endingIndex is
                                    // exclusive, so we need the next index to get the desired
                                    // partialRoute
                                    final Route partialRoute = candidate
                                            .subRoute(fromSubRouteIndex + 1, routeEndIndex + 1);

                                    if (turnRestriction.otherToOptions().stream()
                                            .anyMatch(partialRoute::startsWith))
                                    {
                                        // If the partial route starts with any other to options,
                                        // the only directive has not been followed and this path
                                        // should be restricted
                                        return true;
                                    }
                                }
                                // If the path fully overlaps the "only" restriction, do not return
                                // true, as the path might belong to other restrictions
                                break;
                            case OTHER:
                                // There are some other (not in "No" or "Only") cases that we ignore
                                logger.trace("Not using Other TurnRestrictionType: {}",
                                        turnRestriction.getTurnRestrictionType());
                                return false;
                            default:
                                // Not in the NO, ONLY, or OTHER category: None expected unless
                                // there is an expansion of TurnRestrictionTag enums
                                throw new CoreException("Unknown TurnRestrictionType: {}",
                                        turnRestriction.getTurnRestrictionType());
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param turnRestriction
     *            The {@link TurnRestriction} to use for comparison
     * @param path
     *            The target {@link Route} to examine
     * @return {@code true} if the given {@link Route} contains all parts - via/from/to edges
     */
    private static boolean routeContainsAllTurnRestrictionParts(
            final TurnRestriction turnRestriction, final Route route)
    {
        final Optional<Route> possibleVia = turnRestriction.getVia();
        boolean viaMatches = true;
        if (possibleVia.isPresent())
        {
            viaMatches = route.isSubRoute(possibleVia.get());
        }
        return viaMatches && route.isSubRoute(turnRestriction.getTo())
                && route.isSubRoute(turnRestriction.getFrom());
    }

    private TurnRestriction(final Relation relation)
    {
        Route fromMember = null;
        Route viaMember = null;
        Route toMember = null;
        this.relation = relation;
        this.type = getTurnRestrictionType(relation);
        if (this.type == TurnRestrictionType.OTHER)
        {
            this.from = null;
            this.via = null;
            this.too = null;
            return;
        }
        try
        {
            if (!TurnRestrictionTag.isRestriction(relation))
            {
                throw new CoreException("Relation {} is not a restriction.", relation);
            }
            // Try to re-build the route, based on the "from", "via" and "to" members

            // Build the via members
            final Set<AtlasItem> viaMembers = relation.members().stream()
                    .filter(member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_VIA))
                    .filter(member -> member.getEntity() instanceof Node
                            || member.getEntity() instanceof Edge)
                    .map(RelationMember::getEntity).map(entity -> (AtlasItem) entity)
                    .collect(Collectors.toSet());

            // According to OSM Wiki a restriction relation member can not have more than 1 via node
            // https://wiki.openstreetmap.org/wiki/Relation:restriction#Members
            // If the relation has more than 1 via node then discard the restriction as it is
            // incorrectly modeled.
            // To bring back the turn restriction OSM data needs to be modeled correctly
            final long viaNodeCount = viaMembers.stream()
                    .filter(atlasItem -> atlasItem instanceof Node).count();
            if (viaNodeCount > 1)
            {
                throw new CoreException(
                        "Restriction relation should not have more than 1 via node. But, {} has {} via nodes",
                        relation.getOsmIdentifier(), viaNodeCount);
            }

            // If there are no via members, create a temporary unfiltered set of "to" items, to help
            // with future filtering of "from" items by connectivity.
            final Set<AtlasItem> temporaryToMembers = new HashSet<>();
            if (viaMembers.isEmpty())
            {
                if (isSameRoadViaAndTo(relation))
                {
                    throw new CoreException(
                            "Relation {} has same members in from and to, but has no via members to disambiguate.",
                            relation.getIdentifier());
                }
                relation.members().stream().filter(
                        member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_TO))
                        .forEach(member -> temporaryToMembers.add((AtlasItem) member.getEntity()));
            }

            // Filter the members to extract only the "from" members that are connected at the end
            // to via members if any, or to the start of "to" members.
            final Set<Edge> fromMembers = new TreeSet<>();
            relation.members().stream().filter(member -> member.getRole()
                    .equals(RelationTypeTag.RESTRICTION_ROLE_FROM)
                    && member.getEntity() instanceof Edge
                    && (!viaMembers.isEmpty()
                            && ((Edge) member.getEntity()).isConnectedAtEndTo(viaMembers)
                            || ((Edge) member.getEntity()).isConnectedAtEndTo(temporaryToMembers)))
                    .forEach(member -> fromMembers.add((Edge) member.getEntity()));
            fromMember = Route.fromNonArrangedEdgeSet(fromMembers, false);

            // Filter the members to extract only the "to" members that are connected at the
            // beginning to via members if any, or to the end of "from" members.
            final Set<Edge> toMembers = new TreeSet<>();
            relation.members().stream().filter(member -> member.getRole()
                    .equals(RelationTypeTag.RESTRICTION_ROLE_TO)
                    && member.getEntity() instanceof Edge
                    && (!viaMembers.isEmpty()
                            && ((Edge) member.getEntity()).isConnectedAtStartTo(viaMembers)
                            || ((Edge) member.getEntity()).isConnectedAtStartTo(fromMembers)))
                    .forEach(member -> toMembers.add((Edge) member.getEntity()));
            toMember = Route.fromNonArrangedEdgeSet(toMembers, false);

            // Take only the via members that are edges. Build this last to guarantee a route from
            // from to too.
            final Set<Edge> viaEdges = viaMembers.stream().filter(member -> member instanceof Edge)
                    .map(member -> (Edge) member).collect(Collectors.toSet());

            if (!viaEdges.isEmpty())
            {
                // It's possible that the via edge is bi-directional. To prevent both edges from
                // being put into the route, build a route using all unique edges once. This method
                // attempts to build a route from from the end of from to start of too.
                viaMember = Route.buildFullRouteIgnoringReverseEdges(viaEdges,
                        this.from.end().end(), this.too.start().start());
            }
        }
        catch (final CoreException e)
        {
            logger.trace("Could not build TurnRestriction from relation {}", relation, e);
            fromMember = null;
            viaMember = null;
            toMember = null;
        }
        this.from = fromMember;
        this.via = viaMember;
        this.too = toMember;
    }

    @SuppressWarnings("deprecation")
    public LocationIterableProperties asGeoJson()
    {
        final Map<String, String> tagsNo = Maps.hashMap("highway", "primary", "oneway", "yes",
                "type", "NO");
        final Map<String, String> tagsOnly = Maps.hashMap("highway", "primary", "oneway", "yes",
                "type", "ONLY");
        return new LocationIterableProperties(this.route().asPolyLine(),
                this.getTurnRestrictionType() == TurnRestrictionType.NO ? tagsNo : tagsOnly);
    }

    @Override
    public Rectangle bounds()
    {
        if (!isValid())
        {
            throw new CoreException("An invalid TurnRestriction cannot be Located.");
        }
        return route().bounds();
    }

    /**
     * @return The "from" members of this {@link TurnRestriction}
     */
    public Route getFrom()
    {
        return this.from;
    }

    /**
     * @return The "to" members of this {@link TurnRestriction}
     */
    public Route getTo()
    {
        return this.too;
    }

    public TurnRestrictionType getTurnRestrictionType()
    {
        return this.type;
    }

    public Optional<Route> getVia()
    {
        return Optional.ofNullable(this.via);
    }

    /**
     * @return The {@link TurnRestriction}'s full {@link Route}
     */
    public Route route()
    {
        if (this.route == null)
        {
            final List<Route> routes = new ArrayList<>();
            if (this.from != null)
            {
                routes.add(this.from);
            }
            if (this.via != null)
            {
                routes.add(this.via);
            }
            if (this.too != null)
            {
                routes.add(this.too);
            }
            try
            {
                this.route = Route.forRoutes(routes);
            }
            catch (final Exception e)
            {
                logger.trace("Can't build route from {}", this.relation, e);
            }
        }
        return this.route;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(this.type.toString());
        builder.append(": ");
        if (this.from != null)
        {
            final StringList froms = new StringList();
            this.from.forEach(edge -> froms.add(String.valueOf(edge.getIdentifier())));
            builder.append(froms.join(","));
        }
        builder.append("_");
        if (this.via != null)
        {
            final StringList vias = new StringList();
            this.via.forEach(edge -> vias.add(String.valueOf(edge.getIdentifier())));
            builder.append(vias.join(","));
        }
        builder.append("_");
        if (this.too != null)
        {
            final StringList tos = new StringList();
            this.too.forEach(edge -> tos.add(String.valueOf(edge.getIdentifier())));
            builder.append(tos.join(","));
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return The set of {@link Route} that are emaning from the Via route, but which are not the
     *         current To option.
     */
    protected Set<Route> otherToOptions()
    {
        final Set<Route> result = new HashSet<>();
        for (final Edge toEdge : this.too.start().start().outEdges())
        {
            if (toEdge.equals(this.too.start()))
            {
                continue;
            }
            result.add(Route.forEdge(toEdge));
        }
        return result;
    }

    private boolean isSameRoadViaAndTo(final Relation relation)
    {
        final Set<Long> fromIdentifiers = new TreeSet<>();
        final Set<Long> toIdentifiers = new TreeSet<>();
        relation.members().stream().filter(member -> "to".equals(member.getRole()))
                .forEach(member -> toIdentifiers.add(member.getEntity().getIdentifier()));
        relation.members().stream().filter(member -> "from".equals(member.getRole()))
                .forEach(member -> fromIdentifiers.add(member.getEntity().getIdentifier()));
        return fromIdentifiers.equals(toIdentifiers);
    }

    private boolean isValid()
    {
        return this.from != null && this.too != null && route() != null;
    }
}
