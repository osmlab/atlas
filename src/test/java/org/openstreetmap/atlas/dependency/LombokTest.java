package org.openstreetmap.atlas.dependency;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Needs annotation processing, For IntelliJ IDEA go to Preferences | Build, Execution, Deployment |
 * Compiler | Annotation Processors and then "Enable Annotation Processing".
 *
 * @author Yazad Khambata
 */
public class LombokTest
{

    private static final Logger log = LoggerFactory.getLogger(LombokTest.class);
    private List l = new ArrayList();
    private JButton b = new JButton();

    @Test
    public void sanity()
    {
        System.out.println("" + l + b);

        final String name = "Star Wars";
        // Notice: Builder
        final Movie starWars = Movie.builder().name(name)
                .playFeatures(/* Notice: constructor */new VideoPlayer(3)).build();
        final Movie theOtherStarWars = Movie.builder().name(name).playFeatures(new VideoPlayer(0))
                .build();

        // Notice: Delegation
        Assert.assertTrue(starWars.canSkipAds());
        Assert.assertFalse(theOtherStarWars.canSkipAds());
        Assert.assertEquals(theOtherStarWars.playFeatures.canSkipAds(),
                theOtherStarWars.canSkipAds());

        // Notice: getter
        Assert.assertEquals(name, starWars.getName());

        // Generated equals/hashCode
        Assert.assertEquals(starWars, theOtherStarWars);
        Assert.assertEquals(starWars.hashCode(), theOtherStarWars.hashCode());

        log.info("{}", starWars);

        Assert.assertTrue(starWars.toString().contains(name));
    }

    /**
     * The Movie Class.
     */
    @Builder
    @Getter
    @ToString
    @EqualsAndHashCode(exclude = "playFeatures")
    public static class Movie
    {
        @Delegate
        private final VideoPlayer playFeatures;
        private String name;
    }

    /**
     * The Video Player Class.
     */
    @AllArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class VideoPlayer
    {
        private int premiumLevel;

        public Boolean canSkipAds()
        {
            return getPremiumLevel() > 0;
        }
    }
}
