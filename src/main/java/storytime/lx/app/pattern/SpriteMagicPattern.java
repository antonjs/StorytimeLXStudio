package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storytime.lx.model.LampshadePolygon;
import storytime.lx.model.LampshadeView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@LXCategory("Polygon")
public class SpriteMagicPattern extends LXPattern {
    public BooleanParameter heart = new BooleanParameter("Heart");
    public BooleanParameter pulse = new BooleanParameter("Pulse").setMode(BooleanParameter.Mode.MOMENTARY);

    public BooleanParameter dragonfly = new BooleanParameter("Dragonfly");
    public BooleanParameter flap = new BooleanParameter("Flap");

    public BooleanParameter roses = new BooleanParameter("Roses");
    public BooleanParameter flash = new BooleanParameter("Flash");

    public BooleanParameter lamp = new BooleanParameter("Lamp");
    public BooleanParameter glow = new BooleanParameter("Glow");

    public BooleanParameter mountain = new BooleanParameter("Mountain");

    private final LampshadeView lampshadeView;

    private final List<SpriteEffect> effects = new ArrayList<>();

    public SpriteMagicPattern(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());

        addParameter("heart", this.heart);
        addParameter("pulse", this.pulse);
        addParameter("dragonfly", this.dragonfly);
        addParameter("flap", this.flap);
        addParameter("roses", this.roses);
        addParameter("flash", this.flash);
        addParameter("lamp", this.lamp);
        addParameter("glow", this.glow);
        addParameter("mountain", this.mountain);

        this.pulse.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                if (pulse.isOn()) {
                    SpriteEffect effect = new HeartPulse(lx, lampshadeView);
                    addLayer(effect);
                    effects.add(effect);
                    effect.mod.trigger();
                }
            }
        });
    }

    @Override
    protected void run(double deltaMs) {
        setColors(LXColor.BLACK);

        List<LXPoint> points = new ArrayList<>();
        if (heart.isOn()) points.addAll(lampshadeView.spriteNames.get("Heart").points);
        if (dragonfly.isOn()) points.addAll(lampshadeView.spriteNames.get("Dragonfly").points);
        if (roses.isOn()) points.addAll(lampshadeView.spriteNames.get("Roses").points);
        if (lamp.isOn()) points.addAll(lampshadeView.spriteNames.get("Lamp").points);
        if (mountain.isOn()) points.addAll(lampshadeView.spriteNames.get("Mountain").points);

        for (LXPoint point : points) {
            colors[point.index] = LXColor.WHITE;
        }

        for (SpriteEffect effect : this.effects) {
            if (effect.mod.finished()) {
                this.effects.remove(effect);
                removeLayer(effect);
            }
        }
    }

    static abstract class SpriteEffect extends LXLayer {
        LXPeriodicModulator mod;

        public SpriteEffect(LX lx) {
            super(lx);
        }
    }
    static class HeartPulse extends SpriteEffect {
        private final Logger log = LoggerFactory.getLogger(HeartPulse.class);

        private final int PULSE_STEPS = 5;
        private final LampshadeView view;

        List<LampshadePolygon> seen = new ArrayList<>();
        List<List<LampshadePolygon>> rings = new ArrayList<>();

        protected HeartPulse(LX lx, LampshadeView view) {
            super(lx);
            this.view = view;
            this.seen.addAll(view.spriteNames.get("Heart").polygons);

            this.mod = new LinearEnvelope("Mod", 0, 1, 1000);
            addModulator(this.mod);
            this.mod.setLooping(false);

            rings.add(view.spriteNames.get("Heart").polygons);
            for (int i = 1; i < PULSE_STEPS; i++) {
                Set<LampshadePolygon> next = new HashSet<>();
                for (LampshadePolygon poly : rings.get(i-1)) {
                    next.addAll(poly.neighbours);
                }
                rings.add(next.stream().toList());
            }

            log.info("New heart pulse created");
        }

        @Override
        public void run(double deltaMs) {
            setColors(LXColor.BLACK);

            int step = (int)(this.mod.getValue() * (PULSE_STEPS - 1));
            for (LampshadePolygon poly : this.rings.get(step)) {
                for (LXPoint point : poly.points) {
                    colors[point.index] = LXColor.WHITE;
                }
            }
        }
    }
}
