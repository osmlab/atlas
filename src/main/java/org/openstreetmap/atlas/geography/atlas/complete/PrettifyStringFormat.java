package org.openstreetmap.atlas.geography.atlas.complete;

/**
 * @author lcram
 */
public enum PrettifyStringFormat
{
    MINIMAL_SINGLE_LINE,
    MINIMAL_MULTI_LINE;

    public static final int TRUNCATE_LENGTH = 2000;
    public static final String TRUNCATE_ELLIPSES = "...";
}
