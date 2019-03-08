package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;

/**
 * Tests for {@link BinaryChangeSetDeserializer}.
 *
 * @author mkalender
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public class BinaryChangeSetSerializerTest
{
    private static final Random RANDOM_GENERATOR = new Random();
    private static final List<ItemType> ITEM_TYPES = Arrays.asList(ItemType.values());
    private static long NEW_ITEM_IDENTIFIER = 1L;

    private static ChangeItem generateChangeItem(final ItemType type, final ChangeAction action)
    {
        final List<Location> locations = new ArrayList<>();
        switch (type)
        {
            case AREA:
            case LINE:
            case EDGE:
                final int size = 2 + RANDOM_GENERATOR.nextInt(50);
                for (int i = 0; i < size; i++)
                {
                    locations.add(Location.random(Rectangle.MAXIMUM));
                }
                break;
            case POINT:
            case NODE:
            default:
                locations.add(Location.random(Rectangle.MAXIMUM));
        }

        return new SimpleChangeItem(-1 * NEW_ITEM_IDENTIFIER++, randomString(), type, action,
                locations, RandomTagsSupplier.randomTags(5));
    }

    private static List<ChangeItem> generateChangeItems(final int countPerTypePerAction)
    {
        final List<ChangeItem> changeItems = new ArrayList<>();
        for (final ChangeAction action : ChangeAction.values())
        {
            for (int i = 0; i < countPerTypePerAction; i++)
            {
                changeItems
                        .addAll(generateChangeItemsPerAction(countPerTypePerAction, action, true));
            }
        }
        return changeItems;
    }

    private static List<ChangeItem> generateChangeItemsPerAction(final int countPerTypePerAction,
            final ChangeAction action, final boolean addRelations)
    {
        final List<ChangeItem> changeItems = new ArrayList<>();
        for (final ItemType type : ITEM_TYPES)
        {
            // Skip relations
            if (type == ItemType.RELATION)
            {
                continue;
            }

            for (int i = 0; i < countPerTypePerAction; i++)
            {
                changeItems.addAll(
                        generateChangeItemsPerTypePerAction(countPerTypePerAction, type, action));
            }

        }

        if (addRelations)
        {
            // Generate relation changes
            for (int i = 0; i < countPerTypePerAction; i++)
            {
                changeItems.add(
                        generateRelationChangeItem(countPerTypePerAction, action, changeItems));
            }
        }

        return changeItems;
    }

    private static List<ChangeItem> generateChangeItemsPerTypePerAction(final int count,
            final ItemType type, final ChangeAction action)
    {
        final List<ChangeItem> changeItems = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            changeItems.add(generateChangeItem(type, action));
        }
        return changeItems;
    }

    private static ChangeItem generateRelationChangeItem(final int memberCount,
            final ChangeAction action, final List<ChangeItem> candidateMembers)
    {
        final int candidateSize = candidateMembers.size();
        final List<ChangeItemMember> members = new ArrayList<>();
        for (int i = 0; i < memberCount; i++)
        {
            // Pick a random change item as a member
            // If picked change item is being deleted, then make up a change item member
            final ChangeItem candidateMember = candidateMembers
                    .get(RANDOM_GENERATOR.nextInt(candidateSize));

            // Come up with a role
            // Role is null most of the type
            String role = null;
            if (RANDOM_GENERATOR.nextInt(10) < 4)
            {
                role = randomString();
            }

            if (candidateMember.getAction() != ChangeAction.DELETE)
            {
                members.add(new SimpleChangeItemMember(candidateMember.getIdentifier(), role,
                        candidateMember.getType()));
            }
            else
            {
                members.add(new SimpleChangeItemMember(RANDOM_GENERATOR.nextLong(), role,
                        ITEM_TYPES.get(RANDOM_GENERATOR.nextInt(ITEM_TYPES.size()))));
            }
        }

        final SimpleChangeItem relationChange = new SimpleChangeItem();
        relationChange.setIdentifier(-1 * NEW_ITEM_IDENTIFIER++);
        relationChange.setSourceName(randomString());
        relationChange.setType(ItemType.RELATION);
        relationChange.setAction(action);
        relationChange.setTags(RandomTagsSupplier.randomTags(5));
        relationChange.setMembers(members);
        return relationChange;
    }

    private static String randomString()
    {
        if (RANDOM_GENERATOR.nextBoolean())
        {
            // return null;
        }

        return UUID.randomUUID().toString();
    }

    private static ChangeSet serializeAndDeserialize(final ChangeSet changeSet) throws Exception
    {
        // Serialize
        final File tempFile = File.temporary();
        try (BinaryChangeSetSerializer serializer = new BinaryChangeSetSerializer(tempFile))
        {
            serializer.accept(changeSet);
        }

        // Deserialize
        Optional<ChangeSet> readChangeSet = Optional.empty();
        try (BinaryChangeSetDeserializer deserializer = new BinaryChangeSetDeserializer(tempFile))
        {
            readChangeSet = deserializer.get();
            Assert.assertTrue(readChangeSet.isPresent());
        }

        tempFile.delete();

        return readChangeSet.get();
    }

    private static void validate(final ChangeSet changeSet) throws Exception
    {
        // Serialize and then deserialize
        final ChangeSet readChangeSet = serializeAndDeserialize(changeSet);

        // Validate
        Assert.assertEquals(changeSet, readChangeSet);
    }

    @Test
    public void changeSetWithCreateFromAllNonRelationTypesTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(5, ChangeAction.CREATE, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithDeleteFromAllNonRelationTypesTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(5, ChangeAction.DELETE, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithRandomChangesFromAllTypeAndActions() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItems(1));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithReadFromAllNonRelationTypesTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(5, ChangeAction.READ, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithSingleCreateTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(1, ChangeAction.CREATE, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithSingleDeleteTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(1, ChangeAction.DELETE, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithSingleReadTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(1, ChangeAction.READ, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithSingleUpdateTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(1, ChangeAction.UPDATE, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void changeSetWithUpdateFromAllNonRelationTypesTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());
        changeSet.addAll(generateChangeItemsPerAction(5, ChangeAction.UPDATE, false));

        // Validate
        validate(changeSet);
    }

    @Test
    public void emptyChangeSetTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();

        // Validate
        validate(changeSet);
    }

    @Test
    public void emptyChangeSetWithVersionAndDescriptionTest() throws Exception
    {
        // Create
        final ChangeSet changeSet = new SimpleChangeSet();
        changeSet.setDescription(randomString());
        changeSet.setVersion(randomString());

        // Validate
        validate(changeSet);
    }

    @Test
    public void multipleChangeSetTest() throws Exception
    {
        // Create one
        final ChangeSet aChangeSet = new SimpleChangeSet();
        aChangeSet.setDescription(randomString());
        aChangeSet.setVersion(randomString());

        // Create another
        final ChangeSet anotherChangeSet = new SimpleChangeSet();
        anotherChangeSet.setDescription(randomString());
        anotherChangeSet.setVersion(randomString());
        anotherChangeSet.addAll(generateChangeItemsPerAction(1, ChangeAction.UPDATE, false));

        // Create a third one
        final ChangeSet aThirdChangeSet = new SimpleChangeSet();
        aThirdChangeSet.setDescription(randomString());
        aThirdChangeSet.setVersion(randomString());
        aThirdChangeSet.addAll(generateChangeItems(1));

        // Create a fourth null one
        final ChangeSet aFourthChangeSet = null;

        // Serialize
        final File tempFile = File.temporary();
        try (BinaryChangeSetSerializer serializer = new BinaryChangeSetSerializer(tempFile))
        {
            serializer.accept(aChangeSet);
            serializer.accept(anotherChangeSet);
            serializer.accept(aThirdChangeSet);
            serializer.accept(aFourthChangeSet);
        }

        // Deserialize
        try (BinaryChangeSetDeserializer deserializer = new BinaryChangeSetDeserializer(tempFile))
        {
            // Validate
            Optional<ChangeSet> readChangeSet = deserializer.get();
            Assert.assertTrue(readChangeSet.isPresent());
            Assert.assertEquals(readChangeSet.get(), aChangeSet);

            // Validate
            readChangeSet = deserializer.get();
            Assert.assertTrue(readChangeSet.isPresent());
            Assert.assertEquals(readChangeSet.get(), anotherChangeSet);

            // Validate
            readChangeSet = deserializer.get();
            Assert.assertTrue(readChangeSet.isPresent());
            Assert.assertEquals(readChangeSet.get(), aThirdChangeSet);

            // Validate
            readChangeSet = deserializer.get();
            Assert.assertFalse(readChangeSet.isPresent());
        }

        // Delete temp file
        tempFile.delete();
    }
}
