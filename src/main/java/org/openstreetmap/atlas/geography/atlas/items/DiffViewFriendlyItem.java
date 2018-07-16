package org.openstreetmap.atlas.geography.atlas.items;

/**
 * Any feature that adheres to this interface promises to provide a method that returns a
 * representation of itself suitable for diff viewing. Diff view friendly strings should break
 * individual logical units onto separate lines. They should also avoid use of tab characters, since
 * the tab context in which the string will be displayed is generally unknown until runtime.
 *
 * @author lcram
 */
public interface DiffViewFriendlyItem
{
    String toDiffViewFriendlyString();
}
