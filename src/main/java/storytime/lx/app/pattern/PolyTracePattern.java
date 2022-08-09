package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Click;
import heronarts.lx.parameter.*;
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
    private final Logger logger;

    public enum COLOR_MODE { BW, GRADIENT, RANDOM };
    public final EnumParameter<COLOR_MODE> colorMode = new EnumParameter<>("Color Mode", COLOR_MODE.BW);

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
    int[] pathColors;

    public PolyTracePattern(LX lx) {
        super(lx);
        this.logger = LoggerFactory.getLogger(PolyTracePattern.class);

        this.lampshadeView = new LampshadeView(lx.getModel());
        this.fixedIndex.setRange(0, LXUtils.max(1, lampshadeView.polygons.size()));
        this.pathLength.setRange(0, LXUtils.max(1, lampshadeView.polygons.size()));
        this.pathLength.setValue(320);
//        makePath();

//        addParameter("rate", this.rate);
        addParameter("colormode", this.colorMode);
        addParameter("fixedIndex", this.fixedIndex);
        addParameter("pathLength", this.pathLength);
        addParameter("progress", this.progress);

        LXParameterListener updatePath = new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                makePath();
                makeColors();
            }
        };
        this.colorMode.addListener(updatePath);
        this.fixedIndex.addListener(updatePath);
        this.pathLength.addListener(updatePath);

        startModulator(this.increment);
    }

    public void makeColors() {
        // Depending on colormode, make a list of colors (really shades of grey), so each step on the path
        // is that color and can be colorized.
        this.pathColors = new int[this.polyPath.size()];
        for (int i = 0; i < this.polyPath.size(); i++) {
            int color = LXColor.WHITE;

            switch (this.colorMode.getEnum()) {
                case GRADIENT: color = LXColor.hsb(0, 0, (double)i / this.polyPath.size() * 100); break;
                case RANDOM: color = LXColor.hsb(0, 0, Math.random() * 100); break;
            }

            this.pathColors[i] = color;
        }
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
                color = this.pathColors[i];
            } else if (i < Math.ceil(progress)) {
                // We are currently filling this polygon
                color = LXColor.lerp(LXColor.BLACK, this.pathColors[i], (progress - i) * 1);
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
