package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storytime.lx.model.LampshadePolygon;

import java.util.ArrayList;
import java.util.List;

@LXCategory("Polygon")
public class PolyFillPattern extends PolyTracePattern {
    private final Logger logger = LoggerFactory.getLogger(PolyFillPattern.class);

    public PolyFillPattern(LX lx) {
        super(lx);
        makePath();
    }

    @Override
    public void makePath() {
        this.polyPath.clear();

        List<LampshadePolygon> start = new ArrayList<>();
        start.add(this.lampshadeView.polygons.get(this.fixedIndex.getValuei()));
        this.polyPath.add(start);

        for (int i = 0; i < this.pathLength.getValuei() - 1; i++) {
            List<LampshadePolygon> next = new ArrayList<>();

            for (LampshadePolygon curr : this.polyPath.get(i)) {
                neighbourCheck:
                for (LampshadePolygon neighbour : curr.neighbours) {
                    // Check that this neighbour isn't already in the path anywhere so we don't
                    // overlap / backtrack
                    if (next.contains(neighbour)) break; // XXX: shouldn't this be continue?

                    for (List<LampshadePolygon> previous : this.polyPath) {
                        if (previous.contains(neighbour)) {
                            // In previous path, skip it
                            continue neighbourCheck;
                        }
                    }

                    // This neighbour hasn't yet been visited, add it
                    next.add(neighbour);
                }
            }

            if (next.size() > 0) {
                this.polyPath.add(next);
            } else {
                // We've exhausted all the neighbours we can find, end the path.
                break;
            }
        }

        logger.debug("Created new poly path spanning {} polys", polyPath.size());
    }
}
