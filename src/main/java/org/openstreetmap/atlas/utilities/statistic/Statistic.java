package org.openstreetmap.atlas.utilities.statistic;

/**
 * @author tony
 */
public interface Statistic
{
    /**
     * Updates the internal state of the statistic to reflect the addition of the new value
     *
     * @param value
     *            the new value
     */
    void increment(double value);

    /**
     * Print current statistic to logger
     */
    void summary();
}
