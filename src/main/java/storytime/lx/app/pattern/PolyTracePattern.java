package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Click;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storytime.lx.model.LampshadePolygon;
import storytime.lx.model.LampshadeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Traces a path from polygon to polygon across neighbours.
 *
 */
@LXCategory("Polygon")
public class PolyTracePattern extends LXPattern {
    private final Logger logger = LoggerFactory.getLogger(PolyTracePattern.class);

    public final CompoundParameter rate = (CompoundParameter)
            new CompoundParameter("Rate", 50, 10, 10000)
                    .setExponent(2)
                    .setUnits(LXParameter.Units.MILLISECONDS)
                    .setDescription("Iteration speed through points in the model");

    public final DiscreteParameter fixedIndex = new DiscreteParameter("Fixed", 0, 1)
            .setDescription("Fixed LED point to turn on");

    public final DiscreteParameter pathLength = new DiscreteParameter("Path length", 0, 1)
            .setDescription("Maximum length of path");

    public final CompoundParameter progress = new CompoundParameter("Progress", 0, 1)
                    .setDescription("How far through the path we are.");

    private final Click increment = new Click(rate);
    private int active;

    final LampshadeView lampshadeView;
    final List<List<LampshadePolygon>> polyPath = new ArrayList<>();

    public PolyTracePattern(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());
        this.fixedIndex.setRange(0, LXUtils.max(1, lampshadeView.polygons.size()));
        this.pathLength.setRange(0, LXUtils.max(1, lampshadeView.polygons.size()));
        this.pathLength.setValue(320);
//        makePath();

//        addParameter("rate", this.rate);
        addParameter("fixedIndex", this.fixedIndex);
        addParameter("pathLength", this.pathLength);
        addParameter("progress", this.progress);

        startModulator(this.increment);
    }

    public void makePath() {
        this.polyPath.clear();

        List<LampshadePolygon> start = new ArrayList<>();
        start.add(this.lampshadeView.polygons.get(this.fixedIndex.getValuei()));
        this.polyPath.add(start);

        for (int i = 0; i < this.pathLength.getValuei() - 1; i++) {
            List<LampshadePolygon> next = new ArrayList<>();
            for (LampshadePolygon curr : this.polyPath.get(i)) {
//                LampshadePolygon poly = curr;
//                while (poly.equals(curr) || this.polyPath.get(Math.max(0, i - 1)).contains(poly)) {
//                    poly = curr.neighbours.get((int)(Math.random() * curr.neighbours.size()));
//                }

                next.add(curr.neighbours.get((int)(Math.random() * curr.neighbours.size())));
            }
            this.polyPath.add(next);
        }

        logger.debug("Created new poly path spanning {} polys", polyPath.size());
    }

    @Override
    public void run(double deltaMs) {
        if (model.size == 0) {
            return;
        }

        setColors(LXColor.BLACK);

        int i = 0;
        for (List<LampshadePolygon> step : this.polyPath) {
            double progress = this.progress.getValue() * this.polyPath.size();
            int color = LXColor.WHITE;

            if (i < Math.floor(progress)) {
                // Progress is completely past this polygon
                color = LXColor.WHITE;
            } else if (i < Math.ceil(progress)) {
                // We are currently filling this polygon
//                logger.trace("Part-filling polygon at progress {}, i = {}, brightness = {}", progress, i, (progress-i)*100);
                color = LXColor.gray((progress - i) * 100);
            } else {
                // We are not yet at this polygon
                break;
            }

            for (LampshadePolygon poly : step) {
                for (LXPoint point : poly.points) {
                    colors[point.index] = color;
                }
            }

            ++i;
        }
    }
}
