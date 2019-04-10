package org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.consts.FieldChangeOperation;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;

/**
 * @author Yazad Khambata
 */
public class TagChangeListenerTest
{
    @Test
    public void addTag()
    {
        final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();

        final CompleteArea completeArea = newCompleteArea1();
        completeArea.addTagChangeListener(tagChangeListener);

        preValidation(tagChangeListener);

        final String key = "add";
        final String val = "me";
        completeArea.withAddedTag(key, val);

        final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

        Assert.assertEquals(FieldChangeOperation.ADD, lastEvent.getFieldOperation());
        validateBasicEventFields(completeArea, lastEvent);
        Assert.assertEquals(Pair.of(key, val), lastEvent.getNewValue().get());
    }

    private void validateBasicEventFields(final CompleteArea completeArea,
            final TagChangeEvent lastEvent)
    {
        Assert.assertEquals(completeArea.completeItemType(), lastEvent.getCompleteItemType());
        Assert.assertEquals(completeArea.getIdentifier(), lastEvent.getIdentifier());
    }

    @Test
    public void removeTag()
    {
        final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();

        final CompleteArea completeArea = newCompleteArea1();
        completeArea.addTagChangeListener(tagChangeListener);

        preValidation(tagChangeListener);

        final String key = "removeMe";
        completeArea.withRemovedTag(key);

        final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

        Assert.assertEquals(FieldChangeOperation.REMOVE, lastEvent.getFieldOperation());
        validateBasicEventFields(completeArea, lastEvent);
        Assert.assertEquals(key, lastEvent.getNewValue().get());
    }

    @Test
    public void replaceTag()
    {
        final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();

        final CompleteArea completeArea = newCompleteArea1();
        completeArea.addTagChangeListener(tagChangeListener);

        preValidation(tagChangeListener);

        final String oldKey = "removeMe";
        final String newKey = "add";
        final String newVal = "me";
        completeArea.withReplacedTag(oldKey, newKey, newVal);

        final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

        Assert.assertEquals(FieldChangeOperation.REPLACE, lastEvent.getFieldOperation());
        validateBasicEventFields(completeArea, lastEvent);
        Assert.assertEquals(Triple.of(oldKey, newKey, newVal), lastEvent.getNewValue().get());
    }

    @Test
    public void overwriteTag()
    {
        final TestTagChangeListenerImpl tagChangeListener = getTagChangeListener();

        final CompleteArea completeArea = newCompleteArea1();
        completeArea.addTagChangeListener(tagChangeListener);

        preValidation(tagChangeListener);

        final Map<String, String> tags = new HashMap<String, String>()
        {
            {
                put("aaa1", "bbb1");
                put("aaa2", "bbb2");
                put("aaa3", "bbb3");
            }
        };

        completeArea.withTags(tags);

        final TagChangeEvent lastEvent = basicPostValidation(tagChangeListener);

        Assert.assertEquals(FieldChangeOperation.OVERWRITE, lastEvent.getFieldOperation());
        validateBasicEventFields(completeArea, lastEvent);
        Assert.assertEquals(tags, lastEvent.getNewValue().get());
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

    private CompleteArea newCompleteArea1()
    {
        return new CompleteArea(123L, null, null, null);
    }
}
