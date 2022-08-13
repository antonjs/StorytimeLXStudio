/**
 * Copyright 2020- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.app;

import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.studio.LXStudio;
import heronarts.p4lx.ui.component.UIButton;
import heronarts.p4lx.ui.component.UICollapsibleSection;
import heronarts.p4lx.ui.component.UIKnob;
import processing.core.PApplet;
import storytime.lx.app.DashInputs;

import java.io.File;

/**
 * This is an example top-level class to build and run an LX Studio
 * application via an IDE. The main() method of this class can be
 * invoked with arguments to either run with a full Processing 3 UI
 * or as a headless command-line only engine.
 */
public class LXStudioApp extends PApplet implements LXPlugin {

  private static final String WINDOW_TITLE = "LX Studio";

  private static int WIDTH = 1280;
  private static int HEIGHT = 800;
  private static boolean FULLSCREEN = false;

  private static int WINDOW_X = 0;
  private static int WINDOW_Y = 0;

  private static boolean HAS_WINDOW_POSITION = false;

  @Override
  public void settings() {
    if (FULLSCREEN) {
      fullScreen(PApplet.P3D);
    } else {
      size(WIDTH, HEIGHT, PApplet.P3D);
    }
    pixelDensity(displayDensity());
  }

  @Override
  public void setup() {
    LXStudio.Flags flags = new LXStudio.Flags(this);
    flags.resizable = true;
    flags.useGLPointCloud = false;
    flags.startMultiThreaded = true;
    flags.mediaPath = ".";

    new LXStudio(this, flags);
    this.surface.setTitle(WINDOW_TITLE);
    if (!FULLSCREEN && HAS_WINDOW_POSITION) {
      this.surface.setLocation(WINDOW_X, WINDOW_Y);
    }

  }

  // The Dash Inputs
   public DashInputs dash;

  @Override
  public void initialize(LX lx) {
    // Here is where you should register any custom components or make modifications
    // to the LX engine or hierarchy. This is also used in headless mode, so note that
    // you cannot assume you are working with an LXStudio class or that any UI will be
    // available.

    // Register custom pattern and effect types
    lx.registry.addPattern(heronarts.lx.app.pattern.AppPattern.class);
    lx.registry.addPattern(heronarts.lx.app.pattern.AppPatternWithUI.class);
    lx.registry.addEffect(heronarts.lx.app.effect.AppEffect.class);

    // Create an instance of your global component and register it with the LX engine
    // so that it can be saved and loaded in project files
    this.dash = new DashInputs(lx);
    lx.engine.registerComponent("dash", this.dash);

//    try {
//      LXCompoundModulation mod = new LXCompoundModulation(lx.engine.modulation, dash.intensity, dash.vibe);
//      mod.range.setValue(1);
//      lx.engine.modulation.addModulation(mod);
//    } catch (LXParameterModulation.ModulationException e) {
//      System.out.println(e);
//    }

    // Patterns
    lx.registry.addPattern(storytime.lx.app.pattern.DebugPattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.WorkLightPattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.PolyTestPattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.PolyTracePattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.PolyFillPattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.PolyScrollPattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.PolyScrollSpeedPattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.PolyRandomFillPattern.class);
    lx.registry.addPattern(storytime.lx.app.pattern.SpritePattern.class);

    // Effects
    lx.registry.addEffect(storytime.lx.app.effect.PowerLimiterEffect.class);
    lx.registry.addEffect(storytime.lx.app.effect.OutputGammaEffect.class);
    lx.registry.addEffect(storytime.lx.app.effect.PolyGradientEffect.class);
    lx.registry.addEffect(storytime.lx.app.effect.PolyizeEffect.class);

    System.out.println("Mediapath: " + lx.flags.mediaPath);
  }

  public void initializeUI(LXStudio lx, LXStudio.UI ui) {
    // Here is where you may modify the initial settings of the UI before it is fully
    // built. Note that this will not be called in headless mode. Anything required
    // for headless mode should go in the raw initialize method above.
  }

  public static class UIDash extends UICollapsibleSection {
    public UIDash(LXStudio.UI ui, DashInputs dash) {
      super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 240);
      setTitle("Dash");
      setLayout(Layout.VERTICAL);

      this.newHorizontalContainer(50, 2,
              new UIKnob(dash.intensity),
              new UIKnob(dash.vibe),
              new UIKnob(dash.mood),
              new UIKnob(dash.disco)
      ).addToContainer(this);

      float width = 40;
      this.newHorizontalContainer(50, 2,
              new UIKnob(dash.transition),
              new UIButton(width, width, dash.auto),
              new UIButton(width, width, dash.effect),
              new UIKnob(dash.user)
      ).addToContainer(this);

      this.newHorizontalContainer(50, 2,
              new UIButton(width, width, dash.trigger1).setTriggerable(true),
              new UIButton(width, width, dash.trigger2).setTriggerable(true),
              new UIButton(width, width, dash.trigger3).setTriggerable(true),
              new UIButton(width, width, dash.trigger4).setTriggerable(true)
      ).addToContainer(this);
    }
  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {
    // At this point, the LX Studio application UI has been built. You may now add
    // additional views and components to the UI hierarchy.
    new UIDash(ui, this.dash)
    .addToContainer(ui.leftPane.global);
  }

  @Override
  public void draw() {
    // All handled by core LX engine, do not modify, method exists only so that Processing
    // will run a draw-loop.
  }

  /**
   * Main interface into the program. Two modes are supported, if the --headless
   * flag is supplied then a raw CLI version of LX is used. If not, then we embed
   * in a Processing 4 applet and run as such.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    LX.log("Initializing LX version " + LXStudio.VERSION);
    boolean headless = false;
    File projectFile = null;
    for (int i = 0; i < args.length; ++i) {
      if ("--help".equals(args[i])) {
      } else if ("--headless".equals(args[i])) {
        headless = true;
      } else if ("--fullscreen".equals(args[i]) || "-f".equals(args[i])) {
        FULLSCREEN = true;
      } else if ("--width".equals(args[i]) || "-w".equals(args[i])) {
        try {
          WIDTH = Integer.parseInt(args[++i]);
        } catch (Exception x ) {
          LX.error("Width command-line argument must be followed by integer");
        }
      } else if ("--height".equals(args[i]) || "-h".equals(args[i])) {
        try {
          HEIGHT = Integer.parseInt(args[++i]);
        } catch (Exception x ) {
          LX.error("Height command-line argument must be followed by integer");
        }
      } else if ("--windowx".equals(args[i]) || "-x".equals(args[i])) {
        try {
          WINDOW_X = Integer.parseInt(args[++i]);
          HAS_WINDOW_POSITION = true;
        } catch (Exception x ) {
          LX.error("Window X command-line argument must be followed by integer");
        }
      } else if ("--windowy".equals(args[i]) || "-y".equals(args[i])) {
        try {
          WINDOW_Y = Integer.parseInt(args[++i]);
          HAS_WINDOW_POSITION = true;
        } catch (Exception x ) {
          LX.error("Window Y command-line argument must be followed by integer");
        }
      } else if (args[i].endsWith(".lxp")) {
        try {
          projectFile = new File(args[i]);
        } catch (Exception x) {
          LX.error(x, "Command-line project file path invalid: " + args[i]);
        }
      }
    }
    if (headless) {
      // We're not actually going to run this as a PApplet, but we need to explicitly
      // construct and set the initialize callback so that any custom components
      // will be run
      LX.Flags flags = new LX.Flags();
      flags.initialize = new LXStudioApp();
      if (projectFile == null) {
        LX.log("WARNING: No project filename was specified for headless mode!");
      }
      LX.headless(flags, projectFile);
    } else {
      PApplet.main("heronarts.lx.app.LXStudioApp", args);
    }
  }

}
