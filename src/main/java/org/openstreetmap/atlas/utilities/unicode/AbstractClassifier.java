package org.openstreetmap.atlas.utilities.unicode;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.unicode.Classification.CodeBlock;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * Convenience superclass for Classifier implementations to use that handles everything but the
 * population of the classification lookup and ignore tables
 *
 * @author cstaylor
 */
public abstract class AbstractClassifier implements Classifier
{
    /**
     * Simple implementation of the Classification interface where we are passed the data structures
     * we need (bitset and char collection) and work with those to satisfy our contract methods.
     *
     * @author cstaylor
     */
    private static final class DefaultClassification implements Classification
    {
        private final BitSet classifications;
        private final Collection<Character> unknownCharacters;

        DefaultClassification(final BitSet classifications,
                final Collection<Character> unknownCharacters)
        {
            this.classifications = classifications;
            this.unknownCharacters = Collections.unmodifiableCollection(unknownCharacters);
        }

        @Override
        public int getClassificationCount()
        {
            return this.classifications.cardinality();
        }

        @Override
        public Iterable<Character> getUnclassifiedCharacters()
        {
            return this.unknownCharacters;
        }

        @Override
        public boolean has(final CodeBlock classification)
        {
            return this.classifications.get(classification.ordinal());
        }

        @Override
        public Iterator<CodeBlock> iterator()
        {
            return new AbstractIterator<CodeBlock>()
            {
                private int currentIndex = DefaultClassification.this.classifications.nextSetBit(0);

                @Override
                protected CodeBlock computeNext()
                {
                    if (this.currentIndex < 0 || this.currentIndex == Integer.MAX_VALUE)
                    {
                        return endOfData();
                    }
                    final CodeBlock returnValue = CodeBlock.values()[this.currentIndex];
                    this.currentIndex = DefaultClassification.this.classifications
                            .nextSetBit(this.currentIndex + 1);
                    return returnValue;
                }
            };
        }
    }

    private final RangeMap<Integer, CodeBlock> classificationTable;

    private final BitSet ignoreTable;

    protected AbstractClassifier()
    {
        this.classificationTable = TreeRangeMap.create();
        this.ignoreTable = new BitSet();
    }

    @Override
    public Classification classify(final CharSequence sequence)
    {
        if (sequence == null)
        {
            throw new CoreException("value can't be null");
        }
        final BitSet classifications = new BitSet();
        final List<Character> unknowns = new ArrayList<>();
        sequence.chars().filter(createIgnorePredicate()).forEach(character ->
        {
            final CodeBlock classification = this.classificationTable.get(character);
            if (classification == null)
            {
                unknowns.add((char) character);
            }
            else
            {
                classifications.set(classification.ordinal());
            }
        });
        return new DefaultClassification(classifications, unknowns);
    }

    @Override
    public List<Optional<CodeBlock>> transform(final CharSequence sequence)
    {
        return sequence.chars()
                .mapToObj(character -> Optional.ofNullable(this.classificationTable.get(character)))
                .collect(Collectors.toList());
    }

    protected final void add(final String description, final int start, final int end,
            final CodeBlock classification)
    {
        add(description, Range.closed(start, end), classification);
    }

    protected final void add(final String description, final Range<Integer> range,
            final CodeBlock classification)
    {
        this.classificationTable.put(range, classification);
    }

    protected IntPredicate createIgnorePredicate()
    {
        return item -> !this.ignoreTable.get(item);
    }

    protected final void ignore(final String description, final int index)
    {
        this.ignoreTable.set(index);
    }

    protected final void ignore(final String description, final int start, final int end)
    {
        // Why +1? BitSet treats the last index as exclusive, but we want to keep
        // the values passed as the exact values as shown in the Unicode Character tables
        this.ignoreTable.set(start, end + 1);
    }

    protected abstract AbstractClassifier initialize();

}
