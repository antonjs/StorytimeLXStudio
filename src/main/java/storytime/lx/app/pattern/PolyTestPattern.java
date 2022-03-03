package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Click;
import heronarts.lx.parameter.*;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.pattern.test.TestPattern;
import heronarts.lx.utils.LXUtils;
import storytime.lx.model.LampshadePolygon;
import storytime.lx.model.LampshadeView;

/**
 * Pattern to test the individual stained glass polygons on the lampshade.
 *
 * Code liberally borrowed and derived from heronarts.lx.pattern.test.TestPattern,
 * but not just extended from it due to the visibility of variables we need.
 *
 */
@LXCategory("Tools")
public class PolyTestPattern extends LXPattern {
    public enum Mode {
        ITERATE("Iterate Points"),
        FIXED("Fixed Index"),
        SUBKEY("Model Key");

        private final String string;

        private Mode(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return this.string;
        }
    }

    public final EnumParameter<TestPattern.Mode> mode =
            new EnumParameter<TestPattern.Mode>("Mode", TestPattern.Mode.ITERATE)
                    .setDescription("Which mode of test operation to use");

    public final CompoundParameter rate = (CompoundParameter)
            new CompoundParameter("Rate", 50, 10, 10000)
                    .setExponent(2)
                    .setUnits(LXParameter.Units.MILLISECONDS)
                    .setDescription("Iteration speed through points in the model");

    public final DiscreteParameter fixedIndex = new DiscreteParameter("Fixed", 0, 1)
            .setDescription("Fixed LED point to turn on");

    public final StringParameter subkey =
            new StringParameter("Subkey", LXModel.Tag.STRIP)
                    .setDescription("Sets the type of model object to query for");

    private final Click increment = new Click(rate);
    private int active;

    private final LampshadeView lampshadeView;

    public PolyTestPattern(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());
        this.fixedIndex.setRange(0, LXUtils.max(1, lampshadeView.polygons.size()));

        addParameter("mode", this.mode);
        addParameter("rate", this.rate);
        addParameter("fixedIndex", this.fixedIndex);
        addParameter("subkey", this.subkey);
        startModulator(this.increment);
        setAutoCycleEligible(false);
    }

    @Override
    protected void onModelChanged(LXModel model) {
        // TODO: Update View to be updateable
//        this.lampshadeView = new LampshadeView(lx.getModel());
        this.fixedIndex.setRange(0, LXUtils.max(1, lampshadeView.polygons.size()));
    }

    @Override
    public void run(double deltaMs) {
        if (model.size == 0) {
            return;
        }

        setColors(LXColor.BLACK);

        int polyIndex = 0;
        switch (this.mode.getEnum()) {
            case ITERATE:
                if (this.increment.click()) ++this.active;
                polyIndex = this.active % this.lampshadeView.polygons.size();
                break;
            case FIXED:
                polyIndex = this.fixedIndex.getValuei();
                break;
        }

        LampshadePolygon poly = this.lampshadeView.polygons.get(polyIndex);

        for (LXPoint point : poly.points) {
            colors[point.index] = LXColor.WHITE;
        }
    }
}
