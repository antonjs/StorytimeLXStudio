package storytime.lx.app.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LXCategory("Tools")
public class OutputGammaEffect extends LXEffect {
    private final Logger logger = LoggerFactory.getLogger(OutputGammaEffect.class);

    public final CompoundParameter brightness = new CompoundParameter("Brightness", 1)
            .setDescription("Brightness");

    public final CompoundParameter gamma = new CompoundParameter("Gamma", 1, 0, 3)
            .setDescription("Gamma");

    public final DiscreteParameter ditherBits = new DiscreteParameter("Dither", 2, 0, 8)
            .setDescription("Number of bits to dither in the output");

//    public final CompoundParameter gammaG = new CompoundParameter("G", 1, 0, 3)
//            .setDescription("Green channel gamma");
//    public final CompoundParameter gammaB = new CompoundParameter("B", 1, 0, 3)
//            .setDescription("Blue channel gamma");

    byte[][] r;
    byte[][] g;
    byte[][] b;

    int ditherIndex = 0;
    byte[][][] ditherLUTs;

    public OutputGammaEffect(LX lx) {
        super(lx);
        this.addParameter("brightness", this.brightness);
        this.addParameter("gamma", this.gamma);
        this.addParameter("dither", this.ditherBits);

        this.ditherBits.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                updateLUTs();
            }
        });

        this.gamma.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                updateLUTs();
            }
        });

        updateLUTs();
    }

    void updateLUTs() {
        logger.debug("Regenerating dither LUTs");
        this.ditherLUTs = makeDitherLUTs(this.gamma.getValue(), this.ditherBits.getValuei());

        traceLUT(128, 128);
        traceLUT(128, 129);
        traceLUT(128, 130);

        traceLUT(32, 128);
        traceLUT(32, 129);
        traceLUT(32, 130);
    }

    byte[][] makeGammaLUT(double gamma) {
        byte[][] lut = new byte[256][256];
        for (int b = 0; b < 256; ++b) {
            for (int i = 0; i < 256; ++i) {
                lut[b][i] = (byte)(Math.pow(i / 255.0, gamma) * b);
            }
        }

        return lut;
    }

    /**
     * Make different gamma lookup tables for each dithering state. Note that this approach means that at each
     * frame, each pixel (at the same brightness) will necessarily be in the same dithering state. This is not
     * necessarily as good as eg the FastLED approach, where we can have adjacent pixels be at different states,
     * presumably providing a smoother looking effect (in our application too, probably).
     *
     * Because we're doing this at the output, not at (eg) a fade or something, we won't get the benefit of dithering
     * for those unless we increase the color depth of the whole of LX somehow. We could also use the LUT generated
     * here inside a pattern though...
     *
     * For that reason there might be a benefit to doing the dither LUT separately from gamma, so we can use it
     * independently. OTOH does this mess up the number somehow? Do with gamma for now
     *
     * Based loosely on the dithering code in FastLED and https://github.com/raplin/HexaWS2811/blob/master/gamma.py.
     *
     * @param gamma
     * @param bits
     */
    byte[][][] makeDitherLUTs(double gamma, int bits) {
        int numTables = 1 << bits;
        byte[][][] luts = new byte[numTables][256][256];

        logger.info("Generating dither tables: {} tables for {} bits", numTables, bits);

        for (int table = 0; table < numTables; ++table) { // Which dither state are we in?
            logger.debug("Generating table {}", table);
            // Amount we want to dither is the reverse of the table we're in. This gives us a dither amount that is
            // at 0-256 scale, and interleaved, so the dither signal for a 4 bit tables will look like [0, 128, 64, 192].
            int ditherSignal = 0;
            int tmp = table;

            // Basically we read the low bit of 'table' from right to left (low to high), setting ditherSignal
            // from left to right (high to low).
            for (int i = 0; i < 8; i++) { // Reverse the whole byte
                ditherSignal = ditherSignal << 1;
                if (((int)tmp & 1) == 1) { // If the low bit is a 1
                    ditherSignal |= 1; // Set the matching high bit to one
                }
                tmp = tmp >> 1;
            }

            logger.debug("Dither signal for table {} is {}", table, ditherSignal);

            for (int b = 0; b < 256; ++b) { // Global brightness
                for (int i = 0; i < 256; ++i) { // Pixel channel brightness
                    luts[table][b][i] = (byte) (Math.pow(i / 256.0, gamma) * b + ditherSignal / 256.0);
                }
            }
        }

        return luts;
    }

    void traceLUT(int brightness, int input) {
        byte[] levels = new byte[this.ditherLUTs.length];
        for (int i = 0; i < this.ditherLUTs.length; i++) {
            levels[i] = this.ditherLUTs[i][brightness][input];
        }

        logger.info("Dither trace of {} -> {}: {}", input, brightness, levels);
    }

    @Override
    protected void run(double deltaMs, double enabledAmount) {
        // Use a new dither table every frame, cycling when we have
        // gone through all the tables.
        this.ditherIndex = (this.ditherIndex + 1) % this.ditherLUTs.length;

        int b = (int)(this.brightness.getValue() * 255);
        for (int i = 0; i < colors.length; i++) {
            try {
                colors[i] = LXColor.rgb(
                        this.ditherLUTs[this.ditherIndex][b][LXColor.red(colors[i]) & 0xff],
                        this.ditherLUTs[this.ditherIndex][b][LXColor.green(colors[i]) & 0xff],
                        this.ditherLUTs[this.ditherIndex][b][LXColor.blue(colors[i]) & 0xff]);
            } catch (Exception e) {
                logger.warn("Exception");
            }
        }
    }
}
