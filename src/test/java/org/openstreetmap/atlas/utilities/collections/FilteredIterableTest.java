package org.openstreetmap.atlas.utilities.collections;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sam Gass
 */
public class FilteredIterableTest
{
    private static final Logger logger = LoggerFactory.getLogger(FilteredIterableTest.class);

    @Test
    public void testComplexIdentifier() throws MalformedURLException
    {
        final Function<URL, String> identifier = (final URL urlToIdentify) ->
        {
            return urlToIdentify.getProtocol();
        };
        final List<URL> urls = new ArrayList<>();
        urls.add(new URL("https://github.com"));
        urls.add(new URL("http://github.com"));
        urls.add(new URL("https://test.com"));
        urls.add(new URL("http://test.com"));
        final FilteredIterable<URL, String> filteredIterable = Iterables
                .filter(Iterables.asIterable(urls), new HashSet<String>(), identifier);

        // Non-destructive streaming filter
        final Iterable<URL> filtered1 = Iterables.stream(Iterables.asIterable(urls))
                .filter(Iterables.asSet(Iterables.from("http")), identifier).collect();
        logger.info("{}", Iterables.asList(filtered1));
        Assert.assertEquals(2, Iterables.count(filtered1, i -> 1L));

        // No filtering
        final List<URL> unfiltered = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", unfiltered);
        Assert.assertEquals(4, unfiltered.size());

        // filter HTTP -- note that this is the result of choosing such a generic identifier method,
        // even though we invoke addToFilteredSet() on the much more specific "http://github.com"
        filteredIterable.addToFilteredSet(new URL("http://github.com"));
        final List<URL> filtered2 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered2);
        Assert.assertEquals(2, filtered2.size());

        // filter HTTPS -- note that this is the result of choosing such a generic identifier
        // method, even though we invoke addToFilteredSet() on the much more specific
        // "https://github.com"
        filteredIterable.addToFilteredSet(new URL("https://github.com"));
        final List<URL> filtered3 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered3);
        Assert.assertEquals(0, filtered3.size());
    }

    @Test
    public void testFilteringSimple()
    {
        final List<Integer> values = new ArrayList<>();
        final Function<Integer, Integer> identifier = (final Integer integer) ->
        {
            return integer;
        };
        for (int index = 0; index < 10; index++)
        {
            values.add(index);
        }

        // No filtering
        FilteredIterable<Integer, Integer> filteredIterable = new FilteredIterable<>(
                Iterables.asIterable(values), new HashSet<Integer>(), identifier);
        final List<Integer> unfiltered = Iterables.stream(filteredIterable).collectToList();

        logger.info("{}", unfiltered);
        Assert.assertEquals(10, unfiltered.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
                unfiltered);

        // Filter 5 and 6
        filteredIterable.addToFilteredSet(new Integer(5));
        filteredIterable.addToFilteredSet(new Integer(6));
        final List<Integer> filtered1 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered1);
        Assert.assertEquals(8, filtered1.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2, 3, 4, 7, 8, 9)), filtered1);

        // Filter duplicate
        filteredIterable.addToFilteredSet(new Integer(5));
        filteredIterable.addToFilteredSet(new Integer(5));
        final List<Integer> filtered2 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered2);
        Assert.assertEquals(8, filtered2.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2, 3, 4, 7, 8, 9)), filtered2);

        // Filter non-existent entries
        filteredIterable.addToFilteredSet(new Integer(11));
        filteredIterable.addToFilteredSet(new Integer(12));
        final List<Integer> filtered3 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered3);
        Assert.assertEquals(8, filtered3.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2, 3, 4, 7, 8, 9)), filtered3);

        // Filter down to one
        filteredIterable.addToFilteredSet(new Integer(0));
        filteredIterable.addToFilteredSet(new Integer(1));
        filteredIterable.addToFilteredSet(new Integer(2));
        filteredIterable.addToFilteredSet(new Integer(3));
        filteredIterable.addToFilteredSet(new Integer(4));
        filteredIterable.addToFilteredSet(new Integer(7));
        filteredIterable.addToFilteredSet(new Integer(9));
        final List<Integer> filtered4 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered4);
        Assert.assertEquals(1, filtered4.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(8)), filtered4);

        // Filter all
        filteredIterable.addToFilteredSet(new Integer(8));
        final List<Integer> filtered5 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered5);
        Assert.assertEquals(0, filtered5.size());
        Assert.assertEquals(Iterables.asList(Iterables.emptyIterable(Integer.class)), filtered5);

        // New Iterable, testing filter of the first value (edge case)
        filteredIterable = new FilteredIterable<Integer, Integer>(Iterables.asIterable(values),
                new HashSet<Integer>(), identifier);
        filteredIterable.addToFilteredSet(new Integer(0));
        final List<Integer> filtered6 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered6);
        Assert.assertEquals(9, filtered6.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(1, 2, 3, 4, 5, 6, 7, 8, 9)), filtered6);

        // New Iterable, testing filter of the last value (edge case)
        filteredIterable = new FilteredIterable<Integer, Integer>(Iterables.asIterable(values),
                new HashSet<Integer>(), identifier);
        filteredIterable.addToFilteredSet(new Integer(9));
        final List<Integer> filtered7 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered7);
        Assert.assertEquals(9, filtered7.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2, 3, 4, 5, 6, 7, 8)), filtered7);

        // Empty Iterable (edge case)
        // New Iterable, testing filter of the first value (edge case)
        filteredIterable = new FilteredIterable<Integer, Integer>(Iterables.from(0),
                new HashSet<Integer>(), identifier);
        filteredIterable.addToFilteredSet(new Integer(0));
        final List<Integer> filtered8 = Iterables.stream(filteredIterable).collectToList();
        logger.info("{}", filtered8);
        Assert.assertEquals(0, filtered8.size());
        Assert.assertEquals(Iterables.asList(Iterables.emptyIterable(Integer.class)), filtered8);
    }
}
