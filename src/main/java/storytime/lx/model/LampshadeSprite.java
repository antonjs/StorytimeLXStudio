package storytime.lx.model;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LampshadeSprite {
    public final String name;
    public final List<LXPoint> points;
    public final List<LampshadePolygon> polygons;

    public LampshadeSprite(String name, List<LXPoint> points, List<LampshadePolygon> polygons) {
        this.name = name;
        this.points = Collections.unmodifiableList(points);
        this.polygons = Collections.unmodifiableList(polygons);
    }

    public static LampshadeSprite fromPolys(String name, List<LampshadePolygon> polys) {
        List<LXPoint> points = new ArrayList<>();
        for (LampshadePolygon poly : polys) {
            points.addAll(poly.points);
        }

        return new LampshadeSprite(name, points, polys);
    }
}
