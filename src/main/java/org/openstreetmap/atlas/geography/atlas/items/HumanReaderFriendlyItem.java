package org.openstreetmap.atlas.geography.atlas.items;

/**
 * Any feature that adheres to this interface promises to provide a method that returns a human
 * reader friendly string representation of itself. Human reader friendly strings should break
 * individual logical units onto separate lines. They should also avoid use of tab characters, since
 * the tab context in which the string will be displayed is generally unknown.
 *
 * @author lcram
 */
public interface HumanReaderFriendlyItem
{
    String toHumanReaderFriendlyString();
}
