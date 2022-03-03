package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory("Tools")
public class WorkLightPattern extends LXPattern {
  public final int WORK_LIGHT_COLOR = LXColor.rgb(255, 248, 167);
  public final int TRANSPARENT = LXColor.rgba(0, 0, 0, 0);
  public final int FADE_TIME = 500;

  public final BooleanParameter on = new BooleanParameter("On")
          .setDescription("Turn the work light on")
          .setMode(BooleanParameter.Mode.TOGGLE)
          .setValue(false);

  public final CompoundParameter brightness = new CompoundParameter("Brightness", 1, 1)
          .setDescription("Brightness percentage");

  private final LinearEnvelope fader = new LinearEnvelope(0, 20, FADE_TIME);

  public WorkLightPattern(LX lx) {
    super(lx);
    addParameter("on", this.on);
    addParameter("brightness", this.brightness);
    addModulator("fader", this.fader);

    this.on.addListener((p) -> {
      if (this.on.isOn()) {
        // Mark this pattern as active so if it isn't already
        // we can trigger activation on the tools channel by
        // hitting 'On' via OSC.
        this.fader.setRange(0, 1);
        this.fader.reset().trigger();
      } else {
        // Reset the color array to transparent so we don't
        // block out other channels under the tools channel.
        this.fader.setRange(1, 0);
        this.fader.reset().trigger();
      }
    });

    fader.setLooping(false);
    fader.reset().trigger();
  }


  @Override
  protected void run(double deltaMs) {
    if (!this.on.isOn() && !this.fader.isRunning()) return;

    int color = LXColor.lerp(LXColor.BLACK, WORK_LIGHT_COLOR, this.brightness.getValue()); // Black to light fade for brightness
    color = LXColor.lerp(TRANSPARENT, color, fader.getValue()); // Mix alpha if we're fading in or out.
    setColors(color);
  }

}
