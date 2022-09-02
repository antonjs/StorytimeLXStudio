package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.*;
import heronarts.lx.pattern.LXPattern;
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
public class PolyScrollSpeedPattern extends LXPattern {
    private final Logger logger = LoggerFactory.getLogger(PolyScrollSpeedPattern.class);

    public final float[] beatDivisions = new float[] { 4f, 8f, 16f, 32f, 64f, 128f };
    public enum MODTYPE { LINEAR, SIN };
    public enum MODE { HUE, SATURATION, BRIGHTNESS };

    public final EnumParameter<MODTYPE> modType = new EnumParameter<>("Modulator", MODTYPE.LINEAR);
    public final EnumParameter<MODE> mode = new EnumParameter<>("Mode", MODE.HUE);

    public final CompoundParameter start = new CompoundParameter("Start", 0)
            .setDescription("Where to start the hue or brightness");

    public final CompoundParameter spread = new CompoundParameter("Spread", 1)
                    .setDescription("How much hue or brightness to spread the polys across");

    public final CompoundParameter minSpeed = (CompoundParameter) new CompoundParameter("Min Speed", 1, 0, 60)
            .setExponent(2)
            .setUnits(LXParameter.Units.SECONDS)
            .setDescription("Minimum speed at which to advance a poly color");

    public final CompoundParameter maxSpeed = (CompoundParameter) new CompoundParameter("Max Speed", 1, 0, 60)
            .setExponent(2)
            .setUnits(LXParameter.Units.SECONDS)
            .setDescription("Minimum speed at which to advance a poly color");

    public final BooleanParameter sync = new BooleanParameter("Sync", false)
            .setDescription("Sync to global tempo");

    public final BooleanParameter randomize = new BooleanParameter("Randomize")
            .setDescription("Randomize the polygon colors")
            .setMode(BooleanParameter.Mode.MOMENTARY);

    final LampshadeView lampshadeView;
    final List<LXPeriodicModulator> modulators = new ArrayList<LXPeriodicModulator>();

    public PolyScrollSpeedPattern(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());

        addParameter("modtype", this.modType);
        addParameter("mode", this.mode);
        addParameter("start", this.start);
        addParameter("spread", this.spread);
        addParameter("minspeed", this.minSpeed);
        addParameter("maxspeed", this.maxSpeed);
        addParameter("sync", this.sync);
        addParameter("randomize", this.randomize);

        LXParameterListener speedListener = new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) { updateSpeeds(); }
        };

        minSpeed.addListener(speedListener);
        maxSpeed.addListener(speedListener);
        this.sync.addListener(speedListener);
        this.lx.engine.tempo.period.addListener(speedListener);

        this.modType.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) { setModulators(); }
        });

        this.randomize.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                randomizeModulators();
            }
        });

        setModulators();
    }

    void setModulators() {
        for (LXPeriodicModulator mod : this.modulators) {
            // Remove any existing modulators we might have.
            removeModulator(mod);
        }
        this.modulators.clear();

        for (LampshadePolygon poly : this.lampshadeView.polygons) {
            LXPeriodicModulator mod;
            switch (this.modType.getEnum()) {
                case SIN: mod = new SinLFO(0, 1, 1000); break;
                default: mod = new LinearEnvelope(0, 1, 1000); break;
            }
            mod.setLooping(true);
            mod.start();
            addModulator(mod);
            modulators.add(mod);
        }

        randomizeModulators();
    }

    void updateSpeeds() {
        // Setup some variables for sync if we're using it.
        int minDivision = (int)(minSpeed.getNormalized() * (this.beatDivisions.length-1));
        int maxDivision = (int)(maxSpeed.getNormalized() * (this.beatDivisions.length-1));
        maxDivision = Math.max(maxDivision, minDivision); // Do something reasonable if min > max

        for (LXPeriodicModulator mod : this.modulators) {
            if (this.sync.isOn()) {
                    int index = (int)(minDivision + (maxDivision-minDivision)*Math.random());
                mod.setPeriod(this.lx.engine.tempo.period.getValue() * this.beatDivisions[index]);
            } else {
                mod.setPeriod((minSpeed.getValue() + (maxSpeed.getValue() - minSpeed.getValue()) * Math.random()) * 1000);
            }
        }
    }

    void randomizeModulators() {
        updateSpeeds();
        for (LXPeriodicModulator mod : this.modulators) {
            mod.setValue(Math.random());
            mod.start();
        }
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < lampshadeView.polygons.size(); i++) {
            for (LXPoint point : lampshadeView.polygons.get(i).points) {
                double color = (this.start.getValue() + this.modulators.get(i).getValue() * spread.getValue()) % 1;
                colors[point.index] = LXColor.hsb(
                        mode.getEnum() == MODE.HUE ? color * 360 : this.palette.getHue(),
                        mode.getEnum() == MODE.SATURATION ? color * 100 : this.palette.getSaturation(),
                        mode.getEnum() == MODE.BRIGHTNESS ? color * 100 : this.palette.getBrightness()
                );
            }
        }
    }
}
