package storytime.lx.app.modulator;

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

@LXModulator.Global("Dash Inputs")
public class DashInputs extends LXModulator implements LXOscComponent {
    public final CompoundParameter intensity = new CompoundParameter("Intensity")
            .setDescription("Macro control parameter");

    public final CompoundParameter vibe = new CompoundParameter("Vibe")
            .setDescription("Macro control parameter");

    public final CompoundParameter mood = new CompoundParameter("Mood")
            .setDescription("Macro control parameter");

    public final CompoundParameter disco = new CompoundParameter("Disco")
            .setDescription("Macro control parameter");

    public final CompoundParameter transition = new CompoundParameter("Transition")
            .setDescription("Macro control parameter");

    public final CompoundParameter user = new CompoundParameter("User")
            .setDescription("Macro control parameter");

    public final BooleanParameter auto = new BooleanParameter("Auto")
            .setDescription("Automatic transitions");

    public final BooleanParameter effect = new BooleanParameter("Effect")
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

    public DashInputs() {
        this("dash");
    }

    public DashInputs(String label) {
        super(label);
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

    }

    @Override
    protected double computeValue(double deltaMs) {
        // Not relevant
        return 0;
    }

}
