package storytime.lx.app.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.utils.LXUtils;
import storytime.lx.model.LampshadePolygon;
import storytime.lx.model.LampshadeView;

import javax.swing.plaf.ComponentUI;

@LXCategory("Polygon")
public class PolyGradientEffect extends LXEffect {
    public enum GradientMode { PIXEL, DISTANCE };
    public final EnumParameter<GradientMode> gradMode = new EnumParameter<>("Mode", GradientMode.PIXEL);

    public final CompoundParameter start = new CompoundParameter("Start", 0, 0, 1);
    public final CompoundParameter hue = new CompoundParameter("Hue", 0, -360, 360);
    public final CompoundParameter sat = new CompoundParameter("Saturation", 0, -100, 100);
    public final CompoundParameter bri = new CompoundParameter("Brightness", 0, -100, 100);


    private final LampshadeView lampshadeView;

    public PolyGradientEffect(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());

        addParameter("mode", this.gradMode);
        addParameter("start", this.start);
        addParameter("hue", this.hue);
        addParameter("saturation", this.sat);
        addParameter("brightness", this.bri);

    }

    double distn(LXPoint a, LXPoint b) {
        double dx = a.xn - b.xn;
        double dy = a.yn - b.yn;
        double dz = a.zn - b.zn;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    @Override
    protected void run(double v, double v1) {
        for (LampshadePolygon poly : this.lampshadeView.polygons) {
            int start = (int)(this.start.getValue() * (poly.points.size()-1));

            for (int i = 0; i < poly.points.size(); i++) {
                LXPoint point = poly.points.get((i + start) % poly.points.size());

                int color = colors[point.index];

                double amount;
                switch (this.gradMode.getEnum()) {
                    case DISTANCE: amount = LampshadePolygon.distn(point, poly.points.get(start)) / poly.getRadius(); break;
                    default: amount = (double)i / poly.points.size(); break;
                };

                int target = LXColor.hsb(
                        LXUtils.wrap(LXColor.h(color) + this.hue.getValue() * amount, 0, 360),
                        LXUtils.constrain(LXColor.s(color) + this.sat.getValue() * amount, 0, 100),
                        LXUtils.constrain(LXColor.b(color) + this.bri.getValue() * amount, 0, 100)
                );

                colors[point.index] = target;
            }
         }
    }
}
