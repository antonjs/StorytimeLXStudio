package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.*;
import heronarts.lx.pattern.LXPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storytime.lx.model.LampshadeView;

/**
 * Traces a path from polygon to polygon across neighbours.
 *
 */
@LXCategory("Polygon")
public class PolyScrollPattern extends LXPattern {
    private final Logger logger = LoggerFactory.getLogger(PolyScrollPattern.class);

    public enum MODE { HUE, SATURATION, BRIGHTNESS };

    public final EnumParameter<MODE> mode = new EnumParameter<>("Mode", MODE.HUE);

    public final CompoundParameter start = new CompoundParameter("Start", 0)
            .setDescription("Where to start the hue or brightness");

    public final CompoundParameter spread = new CompoundParameter("Spread", 1)
                    .setDescription("How much hue or brightness to spread the polys across");

    public final BooleanParameter randomize = new BooleanParameter("Randomize")
            .setDescription("Randomize the polygon colors")
            .setMode(BooleanParameter.Mode.MOMENTARY);

    final LampshadeView lampshadeView;
    final double polyOffsets[];

    public PolyScrollPattern(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());
        this.polyOffsets = new double[lampshadeView.polygons.size()];

        addParameter("mode", this.mode);
        addParameter("start", this.start);
        addParameter("spread", this.spread);
        addParameter("randomize", this.randomize);

        this.randomize.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                updateOffsets();
            }
        });

        updateOffsets();
    }

    void updateOffsets() {
        for (int i = 0; i < polyOffsets.length; i++) {
            polyOffsets[i] = Math.random();
        }
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < lampshadeView.polygons.size(); i++) {
            for (LXPoint point : lampshadeView.polygons.get(i).points) {
                double color = (this.start.getValue() + this.polyOffsets[i] * spread.getValue()) % 1;
                colors[point.index] = LXColor.hsb(
                        mode.getEnum() == MODE.HUE ? color * 360 : this.palette.getHue(),
                        mode.getEnum() == MODE.SATURATION ? color * 100 : this.palette.getSaturation(),
                        mode.getEnum() == MODE.BRIGHTNESS ? color * 100 : this.palette.getBrightness()
                );
            }
        }
    }
}
