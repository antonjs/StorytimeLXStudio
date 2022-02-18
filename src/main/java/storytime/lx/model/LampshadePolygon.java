package storytime.lx.model;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

public class LampshadePolygon {
    public final LXPoint[] points;
    public List<LampshadePolygon> neighbours = new ArrayList<>();

    public LampshadePolygon(LXPoint[] points) {
        this.points = points;
    }
}
