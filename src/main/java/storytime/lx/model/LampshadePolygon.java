package storytime.lx.model;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LampshadePolygon {
    public final List<LXPoint> points;
    public List<LampshadePolygon> neighbours = new ArrayList<>();

    public LampshadePolygon(List<LXPoint> points) {
        this.points = Collections.unmodifiableList(points);
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
