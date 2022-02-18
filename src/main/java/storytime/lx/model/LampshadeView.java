package storytime.lx.model;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * View of the lampshade part of the model.
 */

// It would sort of make sense for this to subclass LXView, but the private constructor makes that hard.
public class LampshadeView {
    public final String LAMPSHADE_TAG = "lampshade";

    public final LXView view;
    public final LXPoint[] points;
    public final List<LampshadePolygon> polygons = new ArrayList<>();

    public LampshadeView(LXModel model) {
        this.view = LXView.create(model, LAMPSHADE_TAG, LXView.Normalization.RELATIVE);
        this.points = this.view.points;
    }

    protected void makePolygons() {

    }
}
