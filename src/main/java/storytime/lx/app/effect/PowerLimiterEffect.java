package storytime.lx.app.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Effect which limits total power output of the model.
 *
 * Intended to be run on the Master bus output. The effect calculates the instantaneous power draw
 * of the full LED set, and reduces the master output to ensure that the maximum power is not exceeded
 * by the actual LEDs.
 *
 * The effect will hard-limit power at a certain level, and below that level can act as a compressor, reducing
 * power by a certain soft ratio. This is designed to prevent visible brightness from varying greatly during
 * patterns that have highly-varying power draw.
 *
 * See also https://www.eecs.qmul.ac.uk/~josh/documents/2012/GiannoulisMassbergReiss-dynamicrangecompression-JAES2012.pdf
 * for information on some of the compression that is attempted.
 */
@LXCategory("Tools")
public class PowerLimiterEffect extends LXEffect {
  private final int ONE_SECOND = 1000;
  private final int VOLTAGE = 12;
  private final int HISTORY_SIZE = 60; // Store 1 minute of history

  private double timeSinceLastUpdate = 0;

  private double peakPower = 0;
  private double energyAccumulator = 0;
  private final Queue<Double> peakPowerHistory = new LinkedList<Double>();
  private final Queue<Double> meanPowerHistory = new LinkedList<Double>();

  public final CompoundParameter power = new CompoundParameter("Power", 0, 10000)
          .setDescription("Instantaneous power in Watts");
  public final CompoundParameter current = new CompoundParameter("Power", 0, 10000)
          .setDescription("Instantaneous current in Amps");

  public final CompoundParameter limitCurrent = new CompoundParameter("Limit Current (A)", 200, 1000)
          .setDescription("Hard current limit in amps.");

//  protected final Queue<double> powerHistory;

  public PowerLimiterEffect(LX lx) {
    super(lx);
    this.addParameter("limitcurrent", this.limitCurrent);
  }

  protected double compressPower(double currentPower, double powerLevel, double deltaMs) {
    double attack = 50;
    double release = 1000;

    double dbPower = Math.log10(currentPower);

    // Gain calculation
    double threshold = 24; // dbW, ~= 20A at 12V
    double ratio = 5; // 5:1 compression ratio
    double width = 3 / 2; // dbW

    // We want the control offset anyway for the level generator,
    // so we don't bother adding in the original dbPower signal.
    // ie. dbOffset is 'difference from input signal'.
    double dbOffset = dbPower;
    if (dbPower - threshold > width) {
      // Full compression regime
      dbOffset = threshold + (dbPower - threshold) / ratio;
    } else if (Math.abs(dbOffset - threshold) <= width) {
      // In the knee
      dbOffset = dbOffset + 1/(ratio-1) * Math.pow(dbPower - threshold + width, 2) / 4 * width;
    }
    dbOffset = dbPower - dbOffset;

    // Level detector
    double coeffTime = (dbOffset > powerLevel) ? attack : release;
    double coeff = Math.exp(-deltaMs / coeffTime);
    double dbLevel = coeff * powerLevel + (1 - coeff) * dbOffset;

    return Math.pow(10, dbLevel);
  }

  @Override
  protected void run(double deltaMs, double enabledAmount) {
    // A clever upgrade would be to tag each strip or submodel with its LED type, so we can
    // do the correct power calculation in heterogenous setups. We'd need access to the model
    // though...
    //
    // This is for WS2815

    double limitGain = 1;
    double power = 0.0323 * colors.length;

    if (enabledAmount > 0) {
      // Calculate power used by the lights
      for (int i = 0; i < colors.length; i++) {
        int r = Byte.toUnsignedInt(LXColor.red(colors[i]));
        int g = Byte.toUnsignedInt(LXColor.green(colors[i]));
        int b = Byte.toUnsignedInt(LXColor.blue(colors[i]));
        int max = r > g ? r : g;
        max = max > b ? max : b;

        power += 0.41 * max / 255;
      }

      // Stop power supplies from exploding
      double current = power / VOLTAGE;
      if (current > this.limitCurrent.getValue()) {
        // We are overcurrent and need to limit it.
        limitGain = current / this.limitCurrent.getValue();
        this.lx.engine.output.brightness.setValue(1 / limitGain);
      } else {
        this.lx.engine.output.brightness.setValue(1);
      }

      // Track statistics
      this.peakPower = power > this.peakPower ? power : this.peakPower;
      this.energyAccumulator += power * deltaMs / ONE_SECOND;

      timeSinceLastUpdate += deltaMs;
      if (timeSinceLastUpdate > ONE_SECOND) {
        double meanPower = this.energyAccumulator * (ONE_SECOND / timeSinceLastUpdate);

        System.out.printf("LED power: %.2fW (Peak %.2fW); %.2fA (Peak %.2fA); Gain %.2f %s\n",
          meanPower,
          this.peakPower,
          meanPower / VOLTAGE,
          this.peakPower / VOLTAGE,
          limitGain,
          limitGain > 1 ? " (Limiting)" : "");

        // We have collected 1 second of data, update our history
        this.peakPowerHistory.add(this.peakPower);
        if (this.peakPowerHistory.size() > HISTORY_SIZE) peakPowerHistory.remove();
        this.peakPower = 0;

        this.meanPowerHistory.add(meanPower);
        if (this.meanPowerHistory.size() > HISTORY_SIZE) meanPowerHistory.remove();
        this.energyAccumulator = 0;

        timeSinceLastUpdate = 0;

        this.model.getPath();
      }
    }
  }

}
