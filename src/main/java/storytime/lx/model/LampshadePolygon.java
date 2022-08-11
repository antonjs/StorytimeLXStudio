package storytime.lx.model;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LampshadePolygon {
    public final List<LXPoint> points;
    public List<LampshadePolygon> neighbours = new ArrayList<>();

    private double radius = 1;

    public LampshadePolygon(List<LXPoint> points) {
        this.points = Collections.unmodifiableList(points);

        double maxDistance = 0;
        for (LXPoint pa : points) {
            for (LXPoint pb: points) {
                maxDistance = Math.max(maxDistance, distn(pa, pb));
            }
        }
        this.radius = maxDistance;
    }

    public static double distn(LXPoint a, LXPoint b) {
        double dx = a.xn - b.xn;
        double dy = a.yn - b.yn;
        double dz = a.zn - b.zn;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public double getRadius () {
        return this.radius;
    }

    public void addNeighbour(LampshadePolygon neighbour) {
        if (!neighbours.contains(neighbour)) {
            neighbours.add(neighbour);
        }
    }

    public void addNeighbour(List<LampshadePolygon> neighbours) {
        for (LampshadePolygon poly : neighbours) {
            addNeighbour(poly);
        }
    }
}
