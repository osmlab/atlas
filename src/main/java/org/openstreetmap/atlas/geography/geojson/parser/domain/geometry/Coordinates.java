package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

/**
 * @author Yazad Khambata
 */
public class Coordinates<V> {

    private V value;

    private Coordinates() {
    }

    public static Coordinates<Position> forPoint() {
        return null;
    }

    //...

}
