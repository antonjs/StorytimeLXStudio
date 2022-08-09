package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storytime.lx.model.LampshadePolygon;

import java.util.ArrayList;
import java.util.List;

@LXCategory("Polygon")
public class PolyRandomFillPattern extends PolyTracePattern {
    private final Logger logger = LoggerFactory.getLogger(PolyRandomFillPattern.class);

    public PolyRandomFillPattern(LX lx) {
        super(lx);
        makePath();
    }

    @Override
    public void makePath() {
        this.polyPath.clear();

        List<LampshadePolygon> start = new ArrayList<>();
        start.add(this.lampshadeView.polygons.get(this.fixedIndex.getValuei()));
        this.polyPath.add(start);

        List<LampshadePolygon> polys = new ArrayList<>(this.lampshadeView.polygons);
        for (int i = 0; i < this.pathLength.getValuei() - 1; i++) {
            List<LampshadePolygon> next = new ArrayList<>();
            int index = (int)(Math.random() * (polys.size()-1));
            next.add(polys.get(index));
            polys.remove(index);
            this.polyPath.add(next);
        }

        logger.debug("Created new poly path spanning {} polys", polyPath.size());
    }
}
