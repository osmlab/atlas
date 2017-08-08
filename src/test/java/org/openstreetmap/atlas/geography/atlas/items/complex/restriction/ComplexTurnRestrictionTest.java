package org.openstreetmap.atlas.geography.atlas.items.complex.restriction;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.TurnRestriction.TurnRestrictionType;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.RestrictedPath;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class ComplexTurnRestrictionTest
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexTurnRestrictionTest.class);

    @Rule
    public ComplexTurnRestrictionTestCaseRule rule = new ComplexTurnRestrictionTestCaseRule();

    @Test
    public void testFalsePredicate()
    {
        Assert.assertEquals(0, Iterables.count(new ComplexTurnRestrictionFinder(x -> false)
                .find(this.rule.getAtlasNo(), Finder::ignore), x -> 1L));
    }

    @Test
    public void testNoRestriction()
    {
        final Atlas atlasNo = this.rule.getAtlasNo();
        logger.trace("AtlasNo: {}", atlasNo);
        final Route candidate = Route.forEdges(atlasNo.edge(102), atlasNo.edge(203));
        int counter = 0;
        for (final ComplexTurnRestriction restriction : new ComplexTurnRestrictionFinder()
                .find(atlasNo, Finder::ignore))
        {
            if (restriction.getTurnRestriction().getTurnRestrictionType() == TurnRestrictionType.NO)
            {
                final Route route = restriction.route();
                if (candidate.equals(route))
                {
                    counter++;
                }
            }
        }
        Assert.assertEquals(1, counter);
        int counterRestriction = 0;
        for (final BigNode bigNode : new BigNodeFinder().find(atlasNo, Finder::ignore))
        {
            final Set<RestrictedPath> paths = bigNode.turnRestrictions();
            for (final RestrictedPath path : paths)
            {
                if (path.getRoute().equals(candidate))
                {
                    counterRestriction++;
                }
            }
        }
        Assert.assertEquals(1, counterRestriction);
    }

    @Test
    public void testNullPredicate()
    {
        Assert.assertEquals(1, Iterables.count(
                new ComplexTurnRestrictionFinder(null).find(this.rule.getAtlasNo(), Finder::ignore),
                x -> 1L));
    }

    @Test
    public void testOnlyRestriction()
    {
        final Atlas atlasOnly = this.rule.getAtlasOnly();
        logger.trace("AtlasOnly: {}", atlasOnly);
        final Route candidate = Route.forEdges(atlasOnly.edge(102), atlasOnly.edge(203));
        int counter = 0;
        for (final ComplexTurnRestriction restriction : new ComplexTurnRestrictionFinder()
                .find(atlasOnly, Finder::ignore))
        {
            if (restriction.getTurnRestriction()
                    .getTurnRestrictionType() == TurnRestrictionType.ONLY)
            {
                final Route route = restriction.route();
                if (candidate.equals(route))
                {
                    counter++;
                }
            }
        }
        Assert.assertEquals(1, counter);

        final Set<RestrictedPath> paths = new HashSet<>();
        for (final BigNode bigNode : new BigNodeFinder().find(atlasOnly, Finder::ignore))
        {
            paths.addAll(bigNode.turnRestrictions());
        }
        Assert.assertEquals(3, paths.size());
    }
}
