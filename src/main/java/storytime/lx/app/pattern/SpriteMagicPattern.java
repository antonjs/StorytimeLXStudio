package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
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
    public DiscreteParameter heartColor = new DiscreteParameter("Heart Color", 0, 0, 5);

    public BooleanParameter dragonfly = new BooleanParameter("Dragonfly");
    public DiscreteParameter dragonflyColor = new DiscreteParameter("Dragonfly Color", 0, 0, 5);
    public BooleanParameter flap = new BooleanParameter("Flap");

    public BooleanParameter roses = new BooleanParameter("Roses");
    public DiscreteParameter roseColor = new DiscreteParameter("Rose Color", 0, 0, 5);
    public BooleanParameter flash = new BooleanParameter("Flash");

    public BooleanParameter lamp = new BooleanParameter("Lamp");
    public DiscreteParameter lampColor = new DiscreteParameter("Lamp Color", 0, 0, 5);
    public BooleanParameter glow = new BooleanParameter("Glow");

    public BooleanParameter mountain = new BooleanParameter("Mountain");
    public DiscreteParameter mountainColor = new DiscreteParameter("Mountain Color", 0, 0, 5);

    public enum Background { BLACK, TRANSPARENT };
    public final int TRANSPARENT = LXColor.rgba(0,0,0,0);
    public EnumParameter<Background> bg = new EnumParameter("Background", Background.TRANSPARENT);

    public BooleanParameter palettize = new BooleanParameter("Palletize", false);

    private final LampshadeView lampshadeView;

    private final List<SpriteEffect> effects = new ArrayList<>();

    public SpriteMagicPattern(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());

        addParameter("background", this.bg);
        addParameter("palettize", this.palettize);

        addParameter("heart", this.heart);
        addParameter("heartcolor", this.heartColor);
        addParameter("pulse", this.pulse);

        addParameter("dragonfly", this.dragonfly);
        addParameter("dragonflycolor", this.dragonflyColor);
        addParameter("flap", this.flap);

        addParameter("roses", this.roses);
        addParameter("rosecolor", this.roseColor);
        addParameter("flash", this.flash);

        addParameter("lamp", this.lamp);
        addParameter("lampcolor", this.lampColor);
        addParameter("glow", this.glow);

        addParameter("mountain", this.mountain);
        addParameter("mountaincolor", this.mountainColor);


//        this.pulse.addListener(new LXParameterListener() {
//            @Override
//            public void onParameterChanged(LXParameter parameter) {
//                if (pulse.isOn()) {
//                    SpriteEffect effect = new HeartPulse(lx, lampshadeView);
//                    addLayer(effect);
//                    effects.add(effect);
//                    effect.mod.trigger();
//                }
//            }
//        });
    }

    void setPointColors(List<LXPoint> points, int color) {
        for (LXPoint point : points) {
            colors[point.index] = color;
        }
    }

    @Override
    protected void run(double deltaMs) {
//        switch (this.bg.getEnum()) {
//            case BLACK -> setColors(LXColor.BLACK);
//            case TRANSPARENT -> setColors(TRANSPARENT);
//        }

        setColors(TRANSPARENT);

        if (heart.isOn()) setPointColors(lampshadeView.spriteNames.get("Heart").points, palettize.isOn() ? palette.swatch.getColor(heartColor.getValuei()).getColor() : LXColor.WHITE);
        if (dragonfly.isOn()) setPointColors(lampshadeView.spriteNames.get("Dragonfly").points, palettize.isOn() ? palette.swatch.getColor(dragonflyColor.getValuei()).getColor() : LXColor.WHITE);
        if (roses.isOn()) setPointColors(lampshadeView.spriteNames.get("Roses").points, palettize.isOn() ? palette.swatch.getColor(roseColor.getValuei()).getColor() : LXColor.WHITE);
        if (lamp.isOn()) setPointColors(lampshadeView.spriteNames.get("Lamp").points, palettize.isOn() ? palette.swatch.getColor(lampColor.getValuei()).getColor() : LXColor.WHITE);
        if (mountain.isOn()) setPointColors(lampshadeView.spriteNames.get("Mountain").points, palettize.isOn() ? palette.swatch.getColor(mountainColor.getValuei()).getColor() : LXColor.WHITE);

//        for (SpriteEffect effect : this.effects) {
//            if (effect.mod.finished()) {
//                this.effects.remove(effect);
//                removeLayer(effect);
//            }
//        }
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
