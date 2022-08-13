package storytime.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.pattern.LXPattern;
import storytime.lx.model.LampshadeSprite;
import storytime.lx.model.LampshadeView;

@LXCategory("Polygon")
public class SpritePattern extends LXPattern {
    public ObjectParameter<LampshadeSprite> sprite;

    private final LampshadeView lampshadeView;

    public SpritePattern(LX lx) {
        super(lx);

        this.lampshadeView = new LampshadeView(lx.getModel());
        this.sprite = new ObjectParameter<LampshadeSprite>("Sprite", this.lampshadeView.sprites.toArray(new LampshadeSprite[this.lampshadeView.sprites.size()]));

        addParameter("sprite", this.sprite);
    }

    @Override
    protected void run(double deltaMs) {
        LampshadeSprite sprite = this.sprite.getObject();
        setColors(LXColor.BLACK);
        for (LXPoint point : sprite.points) {
            colors[point.index] = LXColor.WHITE;
        }
    }
}
