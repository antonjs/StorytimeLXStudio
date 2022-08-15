package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.*;
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

  public final BooleanParameter all = new BooleanParameter("All")
          .setDescription("Toggle all ports");

  public final CompoundParameter brightness = new CompoundParameter("Brightness", 1, 1)
          .setDescription("Brightness percentage");
  public final CompoundParameter r = new CompoundParameter("R", 255, 0, 255);
  public final CompoundParameter g = new CompoundParameter("G", 0, 0, 255);
  public final CompoundParameter b = new CompoundParameter("B", 0, 0, 255);

  public final CompoundParameter pixel; // = new CompoundParameter("Pixel", 0, 1);

  public final EnumParameter<MODE> mode = new EnumParameter<MODE>("Mode", MODE.PORT);

  private final LinearEnvelope fader = new LinearEnvelope(0, 20, FADE_TIME);
  private final LampshadeView lampshadeView;

  private boolean wasActive = false;
  private LXPattern prevPattern;


  public DebugPattern(LX lx) {
    super(lx);

    this.lampshadeView = new LampshadeView(lx.getModel());

//    // Make discrete parameters mappable
//    r.setMappable(true);
//    b.setMappable(true);
//    g.setMappable(true);
//    pixel.setMappable(true);

    int maxPortPoints = 0;
    for (int i = 1; i <= NUM_PORTS; i++) {
      BooleanParameter portOn = new BooleanParameter(String.format("%d", i))
              .setDescription(String.format("Port %d is on", i))
              .setMode(BooleanParameter.Mode.TOGGLE)
              .setValue(true);

      ports.add(portOn);

      maxPortPoints = Math.max(maxPortPoints, getPortPoints(i).size());
    }
    this.pixel = new CompoundParameter("Pixel", 0, 0, maxPortPoints);

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

    this.all.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        for (BooleanParameter port : ports) {
          port.setValue(parameter.getValue());
        }
      }
    });

    addParameter("on", this.on);
    addParameter("brightness", this.brightness);
    addParameter("r", this.r);
    addParameter("g", this.g);
    addParameter("b", this.b);
    addParameter("pixel", this.pixel);
    addParameter("mode", this.mode);
    addParameter("all", this.all);

    for (int i = 0; i < NUM_PORTS; i++) {
      addParameter(String.format("port-%d", i), this.ports.get(i));
    }
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

  /**
   * Get the maximum number of pixels across all the enabled ports.
   * @return
   */
  int portPixelCount() {
    int maxPoints = 0;
    for (BooleanParameter port : ports) {
      if (port.isOn()) {
        maxPoints = Math.max(maxPoints, getPortPoints(port.getLabel()).size());
      }
    }

    return maxPoints;
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
      int polyIndex = Math.min((int)(pixel.getValue()), this.lampshadeView.polygons.size()-1);
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

            points.add(modelPoints.get(width - 1)); // Second corner
            points.add(modelPoints.get(width)); // Second corner

            points.add(modelPoints.get(length + width - 1));
            points.add(modelPoints.get(length + width));

            points.add(modelPoints.get(width*2 + length - 1));
            points.add(modelPoints.get(width*2 + length));
          }
        }
      }
    } else if (mode.getEnum() == MODE.PIXEL) {
      for (BooleanParameter port : ports) {
        int pixelIndex = (int)Math.min(pixel.getValue(), (portPixelCount()-1));

        if (port.isOn()) {
          List<LXPoint> portPoints = getPortPoints(port.getLabel());
          if ((int)pixel.getValue() < portPoints.size()) {
            points.add(portPoints.get(pixelIndex));
          }
        }
      }
    }

    for (LXPoint p : points) {
      colors[p.index] = LXColor.lerp(
              LXColor.BLACK,
              LXColor.rgb((int)this.r.getValue(), (int)this.g.getValue(), (int)this.b.getValue()),
              this.brightness.getValue());
    }
  }

}
