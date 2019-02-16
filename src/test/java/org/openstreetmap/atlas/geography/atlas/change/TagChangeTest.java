package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yazad Khambata
 */
public class TagChangeTest
{

    private static final String KEY_MARS = "mars";
    private static final String KEY_OPPORTUNITY = "opportunity";
    private static final String VALUE_ROVER = "rover";
    private static final String KEY_SINGLETON = "singleton";
    private static final String VALUE_ONE = "ONE";
    private static final Logger log = LoggerFactory.getLogger(TagChangeTest.class);
    private static final String ADDED_TAG_KEY = "added";
    private static final String ADDED_TAG_VALUE_1 = "this";
    private static final String ADDED_TAG_VALUE_2 = "that";
    private static final String ADDED_TAG_VALUE_BLANK = "";
    private static final String ADDED_TAG_VALUE_NULL = null;
    private static final String[] EMPTY_TAG_VALUES = { ADDED_TAG_VALUE_BLANK,
            ADDED_TAG_VALUE_NULL };
    private static final int ORIGINAL_TAG_COUNT_1 = 3;
    private static final int ORIGINAL_TAG_COUNT_2 = 2;
    @Rule
    public TagChangeTestRule tagChangeTestRule = new TagChangeTestRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private void checkAllCompleteEntities(final Consumer<CompleteItemType> consumer)
    {
        Arrays.stream(CompleteItemType.values()).forEach(consumer);
    }

    // Insert or Add new Tag Tests - START

    private void insertNewTag(final CompleteItemType completeItemType, final String key,
            final String value)
    {
        final Atlas atlas = tagChangeTestRule.getAtlas();

        final ItemType itemType = completeItemType.getItemType();

        final AtlasEntity originalAtlasEntity = itemType.entityForIdentifier(atlas,
                TagChangeTestRule.ID_1);

        log.info("Original: {}", originalAtlasEntity);

        final Map<String, String> originalTags = originalAtlasEntity.getTags();

        Assert.assertEquals(ORIGINAL_TAG_COUNT_1, originalTags.size());

        final CompleteEntity completeEntity = completeItemType
                .completeEntityFrom(originalAtlasEntity).withAddedTag(key, value);

        final FeatureChange featureChange1 = FeatureChange.add((AtlasEntity) completeEntity);

        final Change change = ChangeBuilder.newInstance().add(featureChange1).get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);

        final AtlasEntity changedAtlasEntity = itemType.entityForIdentifier(changeAtlas,
                TagChangeTestRule.ID_1);

        log.info("Changed:  {}", changedAtlasEntity);

        Assert.assertEquals(changedAtlasEntity.tag(key), value);
        Assert.assertEquals(ORIGINAL_TAG_COUNT_1 + 1, changedAtlasEntity.getTags().size());

        originalTags.forEach((key1, value1) -> Assert.assertEquals(originalAtlasEntity.tag(key1),
                changedAtlasEntity.tag(key1)));
    }

    @Test
    public void testInsertNewTag()
    {
        checkAllCompleteEntities(completeItemType ->
        {
            final String key = ADDED_TAG_KEY;

            final String value = ADDED_TAG_VALUE_1;

            insertNewTag(completeItemType, key, value);
        });
    }

    @Test
    public void testInsertSameTagTwiceOnCompletedNode()
    {
        checkAllCompleteEntities(completeItemType ->
        {
            final Atlas atlas = tagChangeTestRule.getAtlas();

            final ItemType itemType = completeItemType.getItemType();

            final AtlasEntity originalAtlasEntity = itemType.entityForIdentifier(atlas,
                    TagChangeTestRule.ID_1);

            log.info("Original: {}", originalAtlasEntity);

            final Map<String, String> originalTags = originalAtlasEntity.getTags();

            Assert.assertEquals(ORIGINAL_TAG_COUNT_1, originalTags.size());

            final CompleteEntity completeEntity = completeItemType
                    .completeEntityFrom(originalAtlasEntity)
                    .withAddedTag(ADDED_TAG_KEY, ADDED_TAG_VALUE_1)
                    .withAddedTag(ADDED_TAG_KEY, ADDED_TAG_VALUE_2);

            final FeatureChange featureChange1 = FeatureChange.add((AtlasEntity) completeEntity);

            final Change change = ChangeBuilder.newInstance().add(featureChange1).get();

            final Atlas changeAtlas = new ChangeAtlas(atlas, change);

            final AtlasEntity changedAtlasEntity = itemType.entityForIdentifier(changeAtlas,
                    TagChangeTestRule.ID_1);

            log.info("Changed:  {}", changedAtlasEntity);

            Assert.assertEquals(ADDED_TAG_VALUE_2, changedAtlasEntity.tag(ADDED_TAG_KEY));
            Assert.assertEquals(ORIGINAL_TAG_COUNT_1 + 1, changedAtlasEntity.getTags().size());

            originalTags.forEach((key, value) -> Assert.assertEquals(originalAtlasEntity.tag(key),
                    changedAtlasEntity.tag(key)));
        });
    }

    @Test
    public void testInsertSameTagKeyValueTwiceAsTwoCompleteEntities()
    {
        checkAllCompleteEntities(completeItemType ->
        {
            final Atlas atlas = tagChangeTestRule.getAtlas();

            final ItemType itemType = completeItemType.getItemType();

            final AtlasEntity originalAtlasEntity = itemType.entityForIdentifier(atlas,
                    TagChangeTestRule.ID_1);

            log.info("Original: {}", originalAtlasEntity);

            final Map<String, String> originalTags = originalAtlasEntity.getTags();

            Assert.assertEquals(ORIGINAL_TAG_COUNT_1, originalTags.size());

            final CompleteEntity completeEntity1 = completeItemType
                    .completeEntityFrom(originalAtlasEntity)
                    .withAddedTag(ADDED_TAG_KEY, ADDED_TAG_VALUE_1);
            final CompleteEntity completeEntity2 = completeItemType
                    .completeEntityFrom(originalAtlasEntity)
                    .withAddedTag(ADDED_TAG_KEY, ADDED_TAG_VALUE_1);

            final FeatureChange featureChange1 = FeatureChange.add((AtlasEntity) completeEntity1);
            final FeatureChange featureChange2 = FeatureChange.add((AtlasEntity) completeEntity2);

            final Change change = ChangeBuilder.newInstance().add(featureChange1)
                    .add(featureChange2).get();

            final Atlas changeAtlas = new ChangeAtlas(atlas, change);

            final AtlasEntity changedAtlasEntity = itemType.entityForIdentifier(changeAtlas,
                    TagChangeTestRule.ID_1);

            log.info("Changed:  {}", changedAtlasEntity);

            Assert.assertEquals(ADDED_TAG_VALUE_1, changedAtlasEntity.tag(ADDED_TAG_KEY));
            Assert.assertEquals(ORIGINAL_TAG_COUNT_1 + 1, changedAtlasEntity.getTags().size());

            originalTags.forEach((key, value) -> Assert.assertEquals(originalAtlasEntity.tag(key),
                    changedAtlasEntity.tag(key)));
        });
    }

    @Test
    public void testInsertSameTagKeyTwiceWithDifferentValuesAsTwoCompleteEntities()
    {
        checkAllCompleteEntities(completeItemType ->
        {
            final Atlas atlas = tagChangeTestRule.getAtlas();

            try
            {
                final ItemType itemType = completeItemType.getItemType();

                final AtlasEntity originalAtlasEntity = itemType.entityForIdentifier(atlas,
                        TagChangeTestRule.ID_1);

                log.info("Original: {}", originalAtlasEntity);

                final Map<String, String> originalTags = originalAtlasEntity.getTags();

                Assert.assertEquals(ORIGINAL_TAG_COUNT_1, originalTags.size());

                final CompleteEntity completeEntity1 = completeItemType
                        .completeEntityFrom(originalAtlasEntity)
                        .withAddedTag(ADDED_TAG_KEY, ADDED_TAG_VALUE_1);

                final CompleteEntity completeEntity2 = completeItemType
                        .completeEntityFrom(originalAtlasEntity)
                        .withAddedTag(ADDED_TAG_KEY, ADDED_TAG_VALUE_2);

                final FeatureChange featureChange1 = FeatureChange
                        .add((AtlasEntity) completeEntity1);
                final FeatureChange featureChange2 = FeatureChange
                        .add((AtlasEntity) completeEntity2);

                ChangeBuilder.newInstance().add(featureChange1).add(featureChange2).get();
            }
            catch (final CoreException e)
            {
                Assert.assertTrue(e.getMessage()
                        .startsWith("Cannot merge two feature changes FeatureChange"));

                Assert.assertEquals(e.getCause().getClass(), CoreException.class);

                Assert.assertTrue(e.getCause().getMessage()
                        .equals("Attempted merge failed for tags: {added=this, "
                                + "change=me, hello=world, delete=me} and {added=that, change=me, hello=world, "
                                + "delete=me}"));

                return;
            }

            Assert.fail("The test didn't fail - but was expected to fail. completeItemType: "
                    + completeItemType);
        });
    }

    @Test
    public void testInsertNewTagWithEmptyValue()
    {
        for (final String emptyTagValue : EMPTY_TAG_VALUES)
        {
            checkAllCompleteEntities((completeItemType) -> insertNewTag(completeItemType,
                    ADDED_TAG_KEY, emptyTagValue));
        }
    }

    // Insert or Add new Tag Tests - END

    // Update (or Replace) and Upsert Tag - START

    @Test
    public void testUpdateTagKeyValueAndTagValueOnly()
    {
        checkAllCompleteEntities(completeItemType ->
        {

            final ItemType itemType = completeItemType.getItemType();

            final Atlas atlas = tagChangeTestRule.getAtlas();

            final AtlasEntity originalAtlasEntity = atlas.entity(TagChangeTestRule.ID_2, itemType);

            log.info("Original: {}.", originalAtlasEntity);

            Assert.assertEquals(ORIGINAL_TAG_COUNT_2, originalAtlasEntity.getTags().size());

            final CompleteEntity completeEntity = completeItemType
                    .completeEntityFrom(originalAtlasEntity)
                    .withReplacedTag(KEY_MARS, KEY_OPPORTUNITY, VALUE_ROVER)
                    .withReplacedTag(KEY_SINGLETON, KEY_SINGLETON, VALUE_ONE);

            final FeatureChange featureChange = FeatureChange.add((AtlasEntity) completeEntity);

            final Change change = ChangeBuilder.newInstance().add(featureChange).get();

            final Atlas changeAtlas = new ChangeAtlas(atlas, change);

            final AtlasEntity changedAtlasEntity = changeAtlas.entity(TagChangeTestRule.ID_2,
                    itemType);

            log.info("Changed: {}.", changedAtlasEntity);

            Assert.assertEquals(VALUE_ROVER, changedAtlasEntity.tag(KEY_OPPORTUNITY));
            Assert.assertEquals(VALUE_ONE, changedAtlasEntity.tag(KEY_SINGLETON));
            Assert.assertEquals(ORIGINAL_TAG_COUNT_2, changedAtlasEntity.getTags().size());
        });
    }

    @Test
    public void testUpsertTag()
    {
        checkAllCompleteEntities(completeItemType ->
        {

            final ItemType itemType = completeItemType.getItemType();

            final Atlas atlas = tagChangeTestRule.getAtlas();

            final AtlasEntity originalAtlasEntity = atlas.entity(TagChangeTestRule.ID_2, itemType);

            log.info("Original: {}.", originalAtlasEntity);

            Assert.assertEquals(ORIGINAL_TAG_COUNT_2, originalAtlasEntity.getTags().size());

            final CompleteEntity completeEntity = completeItemType
                    .completeEntityFrom(originalAtlasEntity)
                    .withReplacedTag(KEY_OPPORTUNITY, KEY_OPPORTUNITY, VALUE_ROVER);

            final FeatureChange featureChange = FeatureChange.add((AtlasEntity) completeEntity);

            final Change change = ChangeBuilder.newInstance().add(featureChange).get();

            final Atlas changeAtlas = new ChangeAtlas(atlas, change);

            final AtlasEntity changedAtlasEntity = changeAtlas.entity(TagChangeTestRule.ID_2,
                    itemType);

            log.info("Changed: {}.", changedAtlasEntity);

            Assert.assertEquals(VALUE_ROVER, changedAtlasEntity.tag(KEY_MARS));
            Assert.assertEquals(VALUE_ROVER, changedAtlasEntity.tag(KEY_OPPORTUNITY));

            Assert.assertEquals(ORIGINAL_TAG_COUNT_2 + 1, changedAtlasEntity.getTags().size());
        });
    }

    // Update (or Replace) and Upsert Tag - END

    // Delete (or Remove) Tag - START
    @Test
    public void testDeleteTags()
    {
        checkAllCompleteEntities(completeItemType ->
        {

            final ItemType itemType = completeItemType.getItemType();

            final Atlas atlas = tagChangeTestRule.getAtlas();

            final AtlasEntity originalAtlasEntity = atlas.entity(TagChangeTestRule.ID_2, itemType);

            log.info("Original: {}.", originalAtlasEntity);

            Assert.assertEquals(ORIGINAL_TAG_COUNT_2, originalAtlasEntity.getTags().size());

            final CompleteEntity completeEntity = completeItemType
                    .completeEntityFrom(originalAtlasEntity).withRemovedTag(KEY_MARS)
                    .withRemovedTag(KEY_SINGLETON);

            final FeatureChange featureChange = FeatureChange.add((AtlasEntity) completeEntity);

            final Change change = ChangeBuilder.newInstance().add(featureChange).get();

            final Atlas changeAtlas = new ChangeAtlas(atlas, change);

            final AtlasEntity changedAtlasEntity = changeAtlas.entity(TagChangeTestRule.ID_2,
                    itemType);

            log.info("Changed: {}.", changedAtlasEntity);

            Assert.assertEquals(ORIGINAL_TAG_COUNT_2 - 2, changedAtlasEntity.getTags().size());
        });
    }

    @Test
    public void testDeleteNOP()
    {
        checkAllCompleteEntities(completeItemType ->
        {

            final ItemType itemType = completeItemType.getItemType();

            final Atlas atlas = tagChangeTestRule.getAtlas();

            final AtlasEntity originalAtlasEntity = atlas.entity(TagChangeTestRule.ID_2, itemType);

            log.info("Original: {}.", originalAtlasEntity);

            Assert.assertEquals(ORIGINAL_TAG_COUNT_2, originalAtlasEntity.getTags().size());

            final String junk = "XYZ";
            final CompleteEntity completeEntity = completeItemType
                    .completeEntityFrom(originalAtlasEntity).withRemovedTag(KEY_MARS + junk)
                    .withRemovedTag(KEY_SINGLETON + junk);

            final FeatureChange featureChange = FeatureChange.add((AtlasEntity) completeEntity);

            final Change change = ChangeBuilder.newInstance().add(featureChange).get();

            final Atlas changeAtlas = new ChangeAtlas(atlas, change);

            final AtlasEntity changedAtlasEntity = changeAtlas.entity(TagChangeTestRule.ID_2,
                    itemType);

            log.info("Changed: {}.", changedAtlasEntity);

            Assert.assertEquals(ORIGINAL_TAG_COUNT_2, changedAtlasEntity.getTags().size());
            Assert.assertEquals(VALUE_ROVER, changedAtlasEntity.tag(KEY_MARS));
            Assert.assertNotNull(changedAtlasEntity.tag(KEY_SINGLETON));
            Assert.assertTrue(changedAtlasEntity.tag(KEY_SINGLETON).isEmpty());
        });
    }
    // Delete (or Remove) Tag - START

    // Overwrite All Tags - Start
    @Test
    public void testOverwrite()
    {
        checkAllCompleteEntities(completeItemType ->
        {

            final ItemType itemType = completeItemType.getItemType();

            final Atlas atlas = tagChangeTestRule.getAtlas();

            final AtlasEntity originalAtlasEntity1 = atlas.entity(TagChangeTestRule.ID_1, itemType);
            final AtlasEntity originalAtlasEntity2 = atlas.entity(TagChangeTestRule.ID_2, itemType);

            final Map<String, String> tags1 = originalAtlasEntity1.getTags();
            final Map<String, String> tags2 = originalAtlasEntity2.getTags();

            Assert.assertEquals(ORIGINAL_TAG_COUNT_1, tags1.size());
            Assert.assertEquals(ORIGINAL_TAG_COUNT_2, tags2.size());

            final CompleteEntity completeEntity1 = completeItemType
                    .completeEntityFrom(originalAtlasEntity1).withTags(tags2);
            final CompleteEntity completeEntity2 = completeItemType
                    .completeEntityFrom(originalAtlasEntity2).withTags(tags1);

            final FeatureChange featureChange1 = FeatureChange.add((AtlasEntity) completeEntity1);
            final FeatureChange featureChange2 = FeatureChange.add((AtlasEntity) completeEntity2);

            final Change change = ChangeBuilder.newInstance().add(featureChange1)
                    .add(featureChange2).get();

            final Atlas changeAtlas = new ChangeAtlas(atlas, change);

            Assert.assertEquals(tags2,
                    changeAtlas.entity(TagChangeTestRule.ID_1, itemType).getTags());
            Assert.assertEquals(tags1,
                    changeAtlas.entity(TagChangeTestRule.ID_2, itemType).getTags());
        });
    }
    // Overwrite All Tags - End

}
