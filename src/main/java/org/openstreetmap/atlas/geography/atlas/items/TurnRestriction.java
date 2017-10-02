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
import org.openstreetmap.atlas.tags.TurnRestrictionTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSM Turn restriction, modeled from an {@link Atlas}.
 *
 * @author matthieun
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

    private static final long serialVersionUID = 7043701090121113526L;

    private static final Logger logger = LoggerFactory.getLogger(TurnRestriction.class);

    private final Route from;
    private final Route via;
    private final Route too;
    private final TurnRestrictionType type;
    private Route route;
    private final Relation relation;

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
                                 * turnRestriction path (this is the ONLY case) 3. The path include
                                 * an edge which is connected to the via Node 4. The edge from the
                                 * path that matches the from edge comes before the edge which is
                                 * connected to the via node (this matters for u-turn scenarios)
                                 */

                                if (candidate.isSubRoute(from) && !candidate.isSubRoute(path)
                                        && candidate.isSubRouteForAtLeastOneOf(
                                                turnRestriction.otherToOptions())
                                        && candidate.subRouteIndex(from) < candidate
                                                .subRouteIndex(turnRestriction.otherToOptions()))
                                {
                                    // The path does not completely overlap the route, but it
                                    // overlaps the first edge of the "only" restricted route. That
                                    // means it is a restriction as it goes over the beginning of
                                    // the "only" route but does not follow it.
                                    return true;
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
        final boolean viaMatches = possibleVia.isPresent() ? route.isSubRoute(possibleVia.get())
                : true;
        return viaMatches && route.isSubRoute(turnRestriction.getTo())
                && route.isSubRoute(turnRestriction.getFrom());
    }

    private TurnRestriction(final Relation relation)
    {
        // to -> too for checkstyle.
        Route from = null;
        Route via = null;
        Route too = null;
        this.relation = relation;
        this.type = TurnRestrictionTag.isNoPathRestriction(relation) ? TurnRestrictionType.NO
                : TurnRestrictionTag.isOnlyPathRestriction(relation) ? TurnRestrictionType.ONLY
                        : TurnRestrictionType.OTHER;
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
            // Try to re-build the route, based on the "from", "via" and "to" members of the
            // relation

            // Build the via members
            final Set<AtlasItem> viaMembers = new HashSet<>();
            relation.members().stream().filter(member -> "via".equals(member.getRole())
                    && (member.getEntity() instanceof Node || member.getEntity() instanceof Edge))
                    .forEach(member ->
                    {
                        viaMembers.add((AtlasItem) member.getEntity());
                    });

            // If there are no via members, create a temporary unfiltered set of "to" items, to help
            // with future filtering of "from" items by connectivity.
            final Set<AtlasItem> temporaryToMembers = new HashSet<>();
            if (viaMembers.isEmpty())
            {
                if (isSameRoadViaAndTo(relation))
                {
                    throw new CoreException(
                            "This relation has same members in from and to, but has no via members to disambiguate.");
                }
                relation.members().stream().filter(member -> "to".equals(member.getRole()))
                        .forEach(member -> temporaryToMembers.add((AtlasItem) member.getEntity()));
            }

            // Filter the members to extract only the "from" members that are connected at the end
            // to via members if any, or to the start of "to" members.
            final Set<Edge> fromMembers = new TreeSet<>();
            relation.members().stream().filter(member ->
            {
                return "from".equals(member.getRole()) && member.getEntity() instanceof Edge
                        && (!viaMembers.isEmpty()
                                && ((Edge) member.getEntity()).isConnectedAtEndTo(viaMembers)
                                || ((Edge) member.getEntity())
                                        .isConnectedAtEndTo(temporaryToMembers));
            }).forEach(member -> fromMembers.add((Edge) member.getEntity()));
            from = Route.fromNonArrangedEdgeSet(fromMembers, false);

            // Filter the members to extract only the "to" members that are connected at the
            // beginning to via members if any, or to the end of "from" members.
            final Set<Edge> toMembers = new TreeSet<>();
            relation.members().stream().filter(member ->
            {
                return "to".equals(member.getRole()) && member.getEntity() instanceof Edge
                        && (!viaMembers.isEmpty()
                                && ((Edge) member.getEntity()).isConnectedAtStartTo(viaMembers)
                                || ((Edge) member.getEntity()).isConnectedAtStartTo(fromMembers));
            }).forEach(member -> toMembers.add((Edge) member.getEntity()));
            too = Route.fromNonArrangedEdgeSet(toMembers, false);

            // Take only the via members that are edges. Build this last to guarantee a route from
            // from to too.
            final Set<Edge> viaEdges = viaMembers.stream().filter(member -> member instanceof Edge)
                    .map(member -> (Edge) member).collect(Collectors.toSet());

            if (!viaEdges.isEmpty())
            {
                // It's possible that the via edge is bi-directional. To prevent both edges from
                // being put into the route, build a route using all unique edges once. This method
                // attempts to build a route from from the end of from to start of too.
                via = Route.buildFullRouteIgnoringReverseEdges(viaEdges, from.end().end(),
                        too.start().start());
            }
        }
        catch (final CoreException e)
        {
            logger.trace("Could not build TurnRestriction from relation {}", relation, e);
            from = null;
            via = null;
            too = null;
        }
        this.from = from;
        this.via = via;
        this.too = too;
    }

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
        final boolean valid = this.from != null && this.too != null && route() != null;
        return valid;
    }
}
