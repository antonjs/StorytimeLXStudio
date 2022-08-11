package storytime.lx.app.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.utils.LXUtils;
import storytime.lx.model.LampshadePolygon;
import storytime.lx.model.LampshadeView;

import java.util.HashMap;
import java.util.Map;

@LXCategory("Polygon")
public class PolyizeEffect extends LXEffect {
    public enum Mode { MAX, ADD, MEAN, MODE };
    public final EnumParameter<Mode> mode = new EnumParameter<>("Mode", Mode.MAX);

    private final LampshadeView lampshadeView;

    public PolyizeEffect(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());

        addParameter("mode", this.mode);

    }

    @Override
    protected void run(double v, double v1) {
        for (LampshadePolygon poly : this.lampshadeView.polygons) {
            int target = LXColor.BLACK;
            int targetSum = 0;
            Map<Integer, Integer> colorCounts = new HashMap<>();

            for (LXPoint point : poly.points) {
                int color = colors[point.index];

                if (this.mode.getEnum() == Mode.MAX) {
                    target = LXColor.lightest(color, target);
//                    int sum =
//                            LXColor.red(color) & 0xFF
//                                    + LXColor.green(color) & 0xFF
//                                    + LXColor.blue(color) & 0xFF;
//
//                    if (sum > targetSum) {
//                        targetSum = sum;
//                        target = color;
//                    }
                } else if (this.mode.getEnum() == Mode.ADD) {
                    target = LXColor.add(target, color);
                } else if (this.mode.getEnum() == Mode.MEAN) {
                    target = LXColor.add(target, LXColor.hsb(
                            LXColor.h(color) / poly.points.size(),
                            LXColor.s(color) / poly.points.size(),
                            LXColor.b(color) / poly.points.size()
                    ));
                } else if (this.mode.getEnum() == Mode.MODE) {
                    if (!colorCounts.containsKey(color)) {
                        colorCounts.put(color, 0);
                    }

                    colorCounts.put(color, colorCounts.get(color)+1);
                }
            }

            if (this.mode.getEnum() == Mode.MODE) {
                int max = 0;
                for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
                    if (entry.getValue() > max) {
                        max = entry.getValue();
                        target = entry.getKey();
                    }
                }
            }
            for (LXPoint point : poly.points) {
                colors[point.index] = target;
            }
        }
    }
}
