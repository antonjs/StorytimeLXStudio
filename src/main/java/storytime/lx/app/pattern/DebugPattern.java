package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.pattern.LXPattern;
import storytime.lx.model.LampshadeView;

import java.util.ArrayList;
import java.util.List;

@LXCategory("Tools")
public class DebugPattern extends LXPattern {
  public final int WORK_LIGHT_COLOR = LXColor.rgb(255, 248, 167);
  public final int TRANSPARENT = LXColor.rgba(0, 0, 0, 0);
  public final int FADE_TIME = 500;
  public enum MODE { PIXEL, CORNER, POLY, PORT };

  public final int NUM_PORTS = 24;
  public final List<BooleanParameter> ports = new ArrayList<>();

  public final BooleanParameter on = new BooleanParameter("On")
          .setDescription("Turn debug mode on")
          .setMode(BooleanParameter.Mode.TOGGLE)
          .setValue(false);

  public final CompoundParameter brightness = new CompoundParameter("Brightness", 1, 1)
          .setDescription("Brightness percentage");
  public final DiscreteParameter r = new DiscreteParameter("R", 255, 0, 256);
  public final DiscreteParameter g = new DiscreteParameter("G", 0, 0, 256);
  public final DiscreteParameter b = new DiscreteParameter("B", 0, 0, 256);

  public final DiscreteParameter pixel = new DiscreteParameter("Pixel", 0, 1);

  public final EnumParameter<MODE> mode = new EnumParameter<MODE>("Mode", MODE.PORT);

  private final LinearEnvelope fader = new LinearEnvelope(0, 20, FADE_TIME);
  private final LampshadeView lampshadeView;

  private boolean wasActive = false;
  private LXPattern prevPattern;


  public DebugPattern(LX lx) {
    super(lx);

    this.lampshadeView = new LampshadeView(lx.getModel());

    addParameter("on", this.on);
    addParameter("brightness", this.brightness);
    addParameter("r", this.r);
    addParameter("g", this.g);
    addParameter("b", this.b);
    addParameter("pixel", this.pixel);
    addParameter("mode", this.mode);

    addModulator("fader", this.fader);

    // Figure out the maximum number of points any particular port has so we can size the pixel parameter properly.
    int maxPortPoints = this.lampshadeView.polygons.size(); // At a minimum the number of polygons.
    for (int i = 1; i <= NUM_PORTS; i++) {
      BooleanParameter portOn = new BooleanParameter(String.format("%d", i))
              .setDescription(String.format("Port %d is on", i))
              .setMode(BooleanParameter.Mode.TOGGLE)
              .setValue(true);

      ports.add(portOn);
      addParameter(String.format("port-%d", i), portOn);

      maxPortPoints = Math.max(maxPortPoints, getPortPoints(i).size());
    }

    this.pixel.setRange(0, maxPortPoints);

    this.on.addListener((p) -> {
      if (null == this.getChannel()) return; // Avoid crash when this gets called while initializing the pattern.
      if (this.on.isOn()) {
        // Remember the previous active pattern if one was there and on,
        // so we can return to it if we are switched off.
        // We only remember the state if we were already on and active, because otherwise
        // we won't know whether to turn the channel off at the end.
        this.wasActive = this.getChannel().enabled.isOn();
        if (this.wasActive) {
          this.prevPattern = this.getChannel().getActivePattern();
        }

        // Turn on the tools channel if it isn't already.
        this.getChannel().enabled.setValue(true);

        // Make us active as a pattern.
        this.getChannel().goPattern(this);
      } else {
        if (this.wasActive) {
          this.getChannel().goPattern(this.prevPattern);
        } else {
          // Turn off the tools channel.
          this.getChannel().enabled.setValue(false);
        }
      }
    });

    fader.setLooping(false);
    fader.reset().trigger();
  }

  @Override
  public String getPath() {
    return LXChannel.PATH_PATTERN + "/Debug";
  }

  /**
   * Returns the points associated with a particular output port.
   */
  List<LXPoint> getPortPoints(String port) {
    List<LXPoint> points = new ArrayList<>();
    for (LXModel model : this.lx.getModel().sub("port-" + port)) {
      points.addAll(model.getPoints());
    }
    return points;
  }

  List<LXPoint> getPortPoints(int port) {
    return getPortPoints(String.format("%d", port));
  }

  @Override
  protected void run(double deltaMs) {
    if (!this.on.isOn()) {
      setColors(TRANSPARENT);
      return;
    }

    setColors(LXColor.BLACK);
    List<LXPoint> points = new ArrayList<>();

    if (mode.getEnum() == MODE.PORT) {
      for (BooleanParameter port : ports) {
        if (port.isOn()) {
          points.addAll(getPortPoints(port.getLabel()));
        }
      }
    } else if (mode.getEnum() == MODE.POLY) {
      int polyIndex = Math.min(pixel.getValuei(), this.lampshadeView.polygons.size()-1);
      points = this.lampshadeView.polygons.get(polyIndex).points;
    } else if (mode.getEnum() == MODE.CORNER) {
      for (BooleanParameter port : ports) {
        if (port.isOn()) {
          for (LXModel model : this.lx.getModel().sub("port-" + port.getLabel())) {
            List<LXPoint> modelPoints = model.getPoints();
            int length = Integer.parseInt(model.meta("length"));
            int width = Integer.parseInt(model.meta("width"));

            points.add(modelPoints.get(0)); // Start corner
            points.add(modelPoints.get(modelPoints.size()-1));

            points.add(modelPoints.get(length - 1)); // Second corner
            points.add(modelPoints.get(length)); // Second corner

            points.add(modelPoints.get(length + width - 1));
            points.add(modelPoints.get(length + width));

            points.add(modelPoints.get(length*2 + width - 1));
            points.add(modelPoints.get(length*2 + width));
          }
        }
      }
    } else if (mode.getEnum() == MODE.PIXEL) {
      for (BooleanParameter port : ports) {
        if (port.isOn()) {
          List<LXPoint> portPoints = getPortPoints(port.getLabel());
          if (pixel.getValuei() < portPoints.size()) {
            points.add(portPoints.get(pixel.getValuei()));
          }
        }
      }
    }

    for (LXPoint p : points) {
      colors[p.index] = LXColor.rgb(this.r.getValuei(), this.g.getValuei(), this.b.getValuei());
    }
  }

}
