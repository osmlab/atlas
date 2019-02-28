package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.LINE;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.RELATION;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType.CANAL;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType.DITCH;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType.LAKE;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType.POOL;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType.RESERVOIR;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType.RIVER;
import static org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType.SEA;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieu
 * @author Sid
 */
public class ComplexWaterEntityTest extends AbstractWaterIslandTest
{
    private static final Logger logger = LoggerFactory.getLogger(ComplexWaterEntityTest.class);

    private Atlas atlas;

    @Before
    public void setUp()
    {
        this.atlas = this.getAtlasBuilder().get();
        logger.info("{}", this.atlas);
    }

    @Test
    public void testComplexWaterEntities()
    {
        final Map<WaterType, Integer> waterTypeCount = new HashMap<>();
        final Map<ItemType, Integer> itemTypeCount = new HashMap<>();

        final Iterable<ComplexWaterEntity> waterEntities = new ComplexWaterEntityFinder()
                .find(this.atlas, Finder::ignore);
        for (final ComplexWaterEntity waterEntity : waterEntities)
        {
            add(waterTypeCount, waterEntity.getWaterType());
            add(itemTypeCount, waterEntity.getSource().getType());
        }
        logger.info("WaterType Count : {}", waterTypeCount);
        logger.info("ItemType Count : {}", itemTypeCount);

        // Validation
        Assert.assertEquals("Mismatch in total number of waterEntities", 9,
                Iterables.size(waterEntities));

        final Map<WaterType, Integer> expectedWaterTypeCount = new HashMap<>();
        expectedWaterTypeCount.put(LAKE, 1);
        expectedWaterTypeCount.put(SEA, 1);
        expectedWaterTypeCount.put(RESERVOIR, 1);
        expectedWaterTypeCount.put(RIVER, 2);
        expectedWaterTypeCount.put(CANAL, 1);
        expectedWaterTypeCount.put(DITCH, 2);
        expectedWaterTypeCount.put(POOL, 1);

        Arrays.asList(LAKE, SEA, RESERVOIR, RIVER, CANAL, DITCH, POOL).forEach(type ->
        {
            Assert.assertEquals("Mismatch in number of " + type, expectedWaterTypeCount.get(type),
                    waterTypeCount.get(type));
        });

        final Map<ItemType, Integer> expectedItemTypeCount = new HashMap<>();
        expectedItemTypeCount.put(RELATION, 3);
        expectedItemTypeCount.put(AREA, 3);
        expectedItemTypeCount.put(LINE, 3);

        Arrays.asList(RELATION, AREA, LINE).forEach(type ->
        {
            Assert.assertEquals("Mismatch in number of " + type, expectedItemTypeCount.get(type),
                    itemTypeCount.get(type));
        });

    }

    private <T> void add(final Map<T, Integer> typeCount, final T type)
    {
        typeCount.putIfAbsent(type, 0);
        final int count = typeCount.get(type) + 1;
        typeCount.put(type, count);
    }
}
