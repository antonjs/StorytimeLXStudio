package storytime.lx.app;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class DashInputs extends LXComponent implements LXOscComponent {
    public final CompoundParameter intensity = new CompoundParameter("Intensity", 1)
            .setDescription("Macro control parameter");

    public final CompoundParameter vibe = new CompoundParameter("Vibe", 0)
            .setDescription("Macro control parameter");

    public final CompoundParameter mood = new CompoundParameter("Mood", 0)
            .setDescription("Macro control parameter");

    public final CompoundParameter disco = new CompoundParameter("Disco", 0.5)
            .setDescription("Macro control parameter");

    public final CompoundParameter transition = new CompoundParameter("Transition", 0.5)
            .setDescription("Macro control parameter");

    public final CompoundParameter user = new CompoundParameter("User", 0.5)
            .setDescription("Macro control parameter");

    public final BooleanParameter auto = new BooleanParameter("Auto", true)
            .setDescription("Automatic transitions");

    public final BooleanParameter effect = new BooleanParameter("Effect", true)
            .setDescription("Channel effects on");

    public final BooleanParameter trigger1 = new BooleanParameter("1")
            .setMode(BooleanParameter.Mode.MOMENTARY)
            .setDescription("Effect trigger 1");

    public final BooleanParameter trigger2 = new BooleanParameter("2")
            .setMode(BooleanParameter.Mode.MOMENTARY)
            .setDescription("Effect trigger 2");

    public final BooleanParameter trigger3 = new BooleanParameter("3")
            .setMode(BooleanParameter.Mode.MOMENTARY)
            .setDescription("Effect trigger 3");

    public final BooleanParameter trigger4 = new BooleanParameter("4")
            .setMode(BooleanParameter.Mode.MOMENTARY)
            .setDescription("Effect trigger 4");

    public DashInputs(LX lx) {
        super(lx);
        addParameter("intensity", this.intensity);
        addParameter("mood", this.mood);
        addParameter("vibe", this.vibe);
        addParameter("disco", this.disco);
        addParameter("transition", this.transition);
        addParameter("user", this.user);

        addParameter("auto", this.auto);
        addParameter("effect", this.effect);

        addParameter("trigger1", trigger1);
        addParameter("trigger2", trigger2);
        addParameter("trigger3", trigger3);
        addParameter("trigger4", trigger4);

        // Set up listeners to 'modulate' the things we can't modulate because they aren't CompoundParameters,
        // where we don't want a modulator to show up in the UI, or where we need to do something like skip through
        // color palettes...

        // Intensity is brightness. We change it in our gamma processor so that we get the benefit of dithering
        // at intermediate brightness levels.
        this.intensity.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                lx.engine.mixer.masterBus.getEffect("OutputGamma").getParameter("brightness").setValue(parameter.getValue());
            }
        });

        // Vibe changes between snapshots.
        this.vibe.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                int target = (int)(parameter.getValue() * (lx.engine.snapshots.snapshots.size()-1));

                // Abuse the autocycle cursor to step into the next snapshot with the autocycle
                // transition settings, etc.
                if (target != lx.engine.snapshots.autoCycleCursor.getValuei()) {
                    // We want to be looking at a different snapshot that the one we're currently on.
                    lx.engine.snapshots.autoCycleCursor.setValue(target-1); // Set target to one before so we can step to it
                    lx.engine.snapshots.triggerSnapshotCycle.setValue(true);
                }
            }
        });

        // Mood changes between color palettes.
        this.mood.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                int target = (int)(parameter.getValue() * (lx.engine.palette.swatches.size()-1));
                lx.engine.palette.setSwatch(lx.engine.palette.swatches.get(target));
            }
        });

        // Disco sets BPM
        this.disco.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                lx.engine.tempo.bpm.setNormalized(parameter.getValue());
            }
        });

        // Transition alters the time between pattern changes when we're
        // in auto mode.
        this.transition.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                for (LXAbstractChannel channel : lx.engine.mixer.getChannels()) {
                    if (channel instanceof LXChannel) {
                        LXChannel c = (LXChannel)channel;

                        // Multiply by 0.05 because the full range of transition times goes up to like 4 hours.
                        // We want something like 15 minutes. We should also set the time it takes for transitions to
                        // complete to some fraction of this (so longer switch time == longer transition between).
                        c.autoCycleTimeSecs.setNormalized(parameter.getValue() * 0.05);
                        c.transitionTimeSecs.setNormalized(parameter.getValue());
                    }
                }
            }
        });

        this.auto.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                for (LXAbstractChannel channel : lx.engine.mixer.getChannels()) {
                    if (channel instanceof LXChannel) {
                        LXChannel c = (LXChannel)channel;
                        if (c.getLabel() == "Tools") continue;

                        c.autoCycleEnabled.setValue(parameter.getValue());
                    }
                }
            }
        });

        this.effect.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                LXChannel c = (LXChannel) lx.engine.mixer.getChannel("Effect");
                c.enabled.setValue(parameter.getValue());
            }
        });
    }
}
