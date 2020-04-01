package org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.consts.FieldChangeOperation;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;

/**
 * Test for {@link TagChangeListener}.
 *
 * @param <E>
 *            - the {@link CompleteEntity}.
 * @author Yazad Khambata
 */
public class TagChangeListenerTest<E extends CompleteEntity<E>>
{
    @Test
    public void addTag()
    {
        allForEach(completeEntity ->
        {
            final TestTagChangeListenerImplementation tagChangeListener = getTagChangeListener();
            completeEntity.addTagChangeListener(tagChangeListener);

            preValidation(tagChangeListener);

            final String key = "add";
            final String val = "me";
            completeEntity.withAddedTag(key, val);

            final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

            Assert.assertEquals(FieldChangeOperation.ADD, lastEvent.getFieldOperation());
            validateBasicEventFields(completeEntity, lastEvent);
            Assert.assertEquals(Pair.of(key, val), lastEvent.getNewValue().get());
        });
    }

    @Test
    public void overwriteTag()
    {
        allForEach(completeEntity ->
        {
            final TestTagChangeListenerImplementation tagChangeListener = getTagChangeListener();
            completeEntity.addTagChangeListener(tagChangeListener);

            preValidation(tagChangeListener);

            final Map<String, String> tags = new HashMap<String, String>()
            {
                private static final long serialVersionUID = -4353511172908766690L;

                {
                    put("aaa1", "bbb1");
                    put("aaa2", "bbb2");
                    put("aaa3", "bbb3");
                }
            };

            completeEntity.withTags(tags);

            final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

            Assert.assertEquals(FieldChangeOperation.OVERWRITE, lastEvent.getFieldOperation());
            validateBasicEventFields(completeEntity, lastEvent);
            Assert.assertEquals(tags, lastEvent.getNewValue().get());
        });
    }

    @Test
    public void removeTag()
    {
        allForEach(completeEntity ->
        {
            final TestTagChangeListenerImplementation tagChangeListener = getTagChangeListener();
            completeEntity.addTagChangeListener(tagChangeListener);

            preValidation(tagChangeListener);

            final String key = "removeMe";
            completeEntity.withRemovedTag(key);

            final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

            Assert.assertEquals(FieldChangeOperation.REMOVE, lastEvent.getFieldOperation());
            validateBasicEventFields(completeEntity, lastEvent);
            Assert.assertEquals(key, lastEvent.getNewValue().get());
        });
    }

    @Test
    public void replaceTag()
    {
        allForEach(completeEntity ->
        {
            final TestTagChangeListenerImplementation tagChangeListener = getTagChangeListener();
            completeEntity.addTagChangeListener(tagChangeListener);

            preValidation(tagChangeListener);

            final String oldKey = "removeMe";
            final String newKey = "add";
            final String newVal = "me";
            completeEntity.withReplacedTag(oldKey, newKey, newVal);

            final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

            Assert.assertEquals(FieldChangeOperation.REPLACE, lastEvent.getFieldOperation());
            validateBasicEventFields(completeEntity, lastEvent);
            Assert.assertEquals(Triple.of(oldKey, newKey, newVal), lastEvent.getNewValue().get());
        });
    }

    private List<E> all()
    {
        return (List<E>) Arrays.asList(newCompleteNode1(), newCompletePoint1(), newCompleteLine1(),
                newCompleteEdge1(), newCompleteArea1(), newCompleteRelation1());
    }

    private void allForEach(final Consumer<E> consumer)
    {
        allStream().forEach(consumer);
    }

    private Stream<E> allStream()
    {
        return this.all().stream();
    }

    private TagChangeEvent basicPostValidation(
            final TestTagChangeListenerImplementation tagChangeListener)
    {
        Assert.assertEquals(1, tagChangeListener.getCallCount());
        final TagChangeEvent lastEvent = tagChangeListener.getLastEvent();
        Assert.assertNotNull(lastEvent);
        return lastEvent;
    }

    private TestTagChangeListenerImplementation getTagChangeListener()
    {
        return new TestTagChangeListenerImplementation();
    }

    private CompleteArea newCompleteArea1()
    {
        return new CompleteArea(123L, null, null, null);
    }

    private CompleteEdge newCompleteEdge1()
    {
        return new CompleteEdge(123L, null, null, null, null, null);
    }

    private CompleteLine newCompleteLine1()
    {
        return new CompleteLine(123L, null, null, null);
    }

    private CompleteNode newCompleteNode1()
    {
        return new CompleteNode(123L, null, null, null, null, null);
    }

    private CompletePoint newCompletePoint1()
    {
        return new CompletePoint(123L, null, null, null);
    }

    private CompleteRelation newCompleteRelation1()
    {
        return new CompleteRelation(123L, null, null, null, null, null, null, null);
    }

    private void preValidation(final TestTagChangeListenerImplementation tagChangeListener)
    {
        Assert.assertEquals(0, tagChangeListener.getCallCount());
        Assert.assertNull(tagChangeListener.getLastEvent());
    }

    private void validateBasicEventFields(final CompleteEntity completeEntity,
            final TagChangeEvent lastEvent)
    {
        Assert.assertEquals(completeEntity.completeItemType(), lastEvent.getCompleteItemType());
        Assert.assertEquals(completeEntity.getIdentifier(), lastEvent.getIdentifier());
    }
}
