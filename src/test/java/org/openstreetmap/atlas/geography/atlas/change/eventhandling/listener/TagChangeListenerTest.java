package org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @author Yazad Khambata
 */
public class TagChangeListenerTest
{
    @Test
    public void addTag()
    {
        this.all().stream().forEach(completeEntity ->
        {
            final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();
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

    private void validateBasicEventFields(final CompleteEntity completeEntity,
            final TagChangeEvent lastEvent)
    {
        Assert.assertEquals(completeEntity.completeItemType(), lastEvent.getCompleteItemType());
        Assert.assertEquals(completeEntity.getIdentifier(), lastEvent.getIdentifier());
    }

    @Test
    public void removeTag()
    {
        this.all().stream().forEach(completeEntity ->
        {
            final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();
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
        this.all().stream().forEach(completeEntity ->
        {
            final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();
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

    @Test
    public void overwriteTag()
    {
        this.all().stream().forEach(completeEntity ->
        {
            final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();
            completeEntity.addTagChangeListener(tagChangeListener);

            preValidation(tagChangeListener);

            final Map<String, String> tags = new HashMap<String, String>()
            {
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

    private void preValidation(final TestTagChangeListenerImpl tagChangeListener)
    {
        Assert.assertEquals(0, tagChangeListener.getCallCount());
        Assert.assertNull(tagChangeListener.getLastEvent());
    }

    private TagChangeEvent basicPostValidation(final TestTagChangeListenerImpl tagChangeListener)
    {
        Assert.assertEquals(1, tagChangeListener.getCallCount());
        final TagChangeEvent lastEvent = tagChangeListener.getLastEvent();
        Assert.assertNotNull(lastEvent);
        return lastEvent;
    }

    private TestTagChangeListenerImpl getTagChangeListener()
    {
        return new TestTagChangeListenerImpl();
    }

    private <E extends CompleteEntity<E>> List<E> all()
    {
        return (List<E>) Arrays.asList(newCompleteNode1(), newCompletePoint1(), newCompleteLine1(),
                newCompleteEdge1(), newCompleteArea1(), newCompleteRelation1());
    }

    private CompleteNode newCompleteNode1()
    {
        return new CompleteNode(123L, null, null, null, null, null);
    }

    private CompletePoint newCompletePoint1()
    {
        return new CompletePoint(123L, null, null, null);
    }

    private CompleteLine newCompleteLine1()
    {
        return new CompleteLine(123L, null, null, null);
    }

    private CompleteEdge newCompleteEdge1()
    {
        return new CompleteEdge(123L, null, null, null, null, null);
    }

    private CompleteArea newCompleteArea1()
    {
        return new CompleteArea(123L, null, null, null);
    }

    private CompleteRelation newCompleteRelation1()
    {
        return new CompleteRelation(123L, null, null, null, null, null, null, null);
    }
}
