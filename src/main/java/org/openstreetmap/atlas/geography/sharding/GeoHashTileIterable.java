package org.openstreetmap.atlas.geography.sharding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;

/**
 * @author matthieun
 */
class GeoHashTileIterable implements Iterable<GeoHashTile>
{
    /**
     * @author matthieun
     */
    private class GeoHashTileIterator implements Iterator<GeoHashTile>
    {
        private final char[] current = Arrays.copyOf(GeoHashTileIterable.this.starting,
                GeoHashTileIterable.this.precision);
        private boolean done = false;
        private boolean firstCall = true;
        private int backIndex = GeoHashTileIterable.this.precision - 1;

        @Override
        public boolean hasNext()
        {
            return !this.done;
        }

        @Override
        public GeoHashTile next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            GeoHashTile result = new GeoHashTile(String.valueOf(this.current));
            if (!isMaximum() && this.firstCall)
            {
                while (!this.done && !overlaps(result))
                {
                    upgrade();
                    result = new GeoHashTile(String.valueOf(this.current));
                }
            }
            upgrade();
            while (!isMaximum() && !this.done
                    && !overlaps(new GeoHashTile(String.valueOf(this.current))))
            {
                upgrade();
            }
            this.firstCall = false;
            return result;
        }

        private boolean overlaps(final GeoHashTile tile)
        {
            return GeoHashTileIterable.this.surface.overlaps(tile.bounds());
        }

        private void tick(final int index)
        {
            final char currentChar = this.current[index];
            final int currentCharIndex = GeoHashTile.GEOHASH_CHARACTER_MAP.inverse()
                    .get(currentChar);
            this.current[index] = GeoHashTile.GEOHASH_CHARACTER_MAP.get(currentCharIndex + 1);
        }

        private void upgrade()
        {
            final char lastCharacter = GeoHashTile.GEOHASH_CHARACTERS[GeoHashTile.GEOHASH_CHARACTERS.length
                    - 1];
            while (this.backIndex >= GeoHashTileIterable.this.prefix.length
                    && this.current[this.backIndex] == lastCharacter)
            {
                this.backIndex--;
            }
            if (this.backIndex < GeoHashTileIterable.this.prefix.length)
            {
                this.done = true;
                return;
            }
            tick(this.backIndex);
            if (this.backIndex < GeoHashTileIterable.this.precision - 1)
            {
                zero(this.backIndex + 1);
                this.backIndex = GeoHashTileIterable.this.precision - 1;
            }
        }

        private void zero(final int index)
        {
            for (int subIndex = index; subIndex < GeoHashTileIterable.this.precision; subIndex++)
            {
                this.current[subIndex] = GeoHashTile.GEOHASH_CHARACTERS[0];
            }
        }
    }

    private final char[] starting;
    private final char[] prefix;
    private final int precision;
    private final GeometricSurface surface;
    private final boolean isMaximum;

    GeoHashTileIterable(final int precision)
    {
        this(precision, Rectangle.MAXIMUM);
    }

    GeoHashTileIterable(final int precision, final GeometricSurface surface)
    {
        this.precision = precision;
        this.surface = surface;
        this.starting = new char[precision];
        this.prefix = prefix();
        this.isMaximum = surface instanceof Rectangle && Rectangle.MAXIMUM.equals(surface);
        for (int index = 0; index < this.prefix.length; index++)
        {
            this.starting[index] = this.prefix[index];
        }
        for (int index = this.prefix.length; index < precision; index++)
        {
            this.starting[index] = GeoHashTile.GEOHASH_CHARACTERS[0];
        }
    }

    @Override
    public Iterator<GeoHashTile> iterator()
    {
        return new GeoHashTileIterator();
    }

    private boolean isMaximum()
    {
        return this.isMaximum;
    }

    /**
     * @return The prefix of geohash that totally covers the bounds of the surface
     */
    private char[] prefix() // NOSONAR
    {
        if (isMaximum())
        {
            return new char[0];
        }
        final List<char[]> geoHashes = new ArrayList<>();
        for (final Location corner : this.surface.bounds())
        {
            geoHashes.add(GeoHashTile.covering(corner, this.precision).toCharArray());
        }
        final List<Character> prefixAsList = new ArrayList<>();
        for (int index = 0; index < this.precision; index++)
        {
            Character candidate = null;
            boolean valid = true;
            for (int cornerIndex = 0; cornerIndex < geoHashes.size(); cornerIndex++)
            {
                final char cornerCharacter = geoHashes.get(cornerIndex)[index];
                if (candidate == null)
                {
                    candidate = cornerCharacter;
                }
                else if (!candidate.equals(cornerCharacter))
                {
                    valid = false;
                    break;
                }

            }
            if (valid)
            {
                prefixAsList.add(candidate);
            }
            else
            {
                break;
            }
        }
        final char[] result = new char[prefixAsList.size()];
        for (int index = 0; index < prefixAsList.size(); index++)
        {
            result[index] = prefixAsList.get(index);
        }
        return result;
    }
}
