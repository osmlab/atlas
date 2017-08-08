package org.openstreetmap.atlas.utilities.unicode;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.unicode.Classification.CodeBlock;

/**
 * Test case for the loading classifier
 *
 * @author cstaylor
 */
public class LoadingClassifierTestCase
{
    @Test
    public void koreanAndEnglish()
    {
        final Classification classification = new LoadingClassifier()
                .classify("영일만대로 (Yeongilman-daero)");
        Assert.assertTrue(classification.getClassificationCount() == 2);
        Assert.assertTrue(classification.has(CodeBlock.LATIN));
        Assert.assertTrue(classification.has(CodeBlock.HANGUL));
    }

    @Test
    public void testDefaults()
    {
        final Classification classification = new LoadingClassifier().classify("ABC");
        Assert.assertEquals(1, classification.getClassificationCount());
        Assert.assertTrue(classification.has(CodeBlock.LATIN));
    }

    @Test
    public void testIgnore()
    {
        final Classification classification = new LoadingClassifier().classify("|");
        Assert.assertEquals(0, classification.getClassificationCount());
        Assert.assertFalse(classification.has(CodeBlock.LATIN));
    }

    @Test
    public void testSquareBracketsAndBackSlash()
    {
        final Classification classification = new LoadingClassifier().classify("[]\\");
        Assert.assertEquals(0, classification.getClassificationCount());
        Assert.assertFalse(classification.has(CodeBlock.LATIN));
    }
}
