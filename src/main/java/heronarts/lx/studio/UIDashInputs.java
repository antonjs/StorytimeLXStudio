package heronarts.lx.studio;

import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIButton;
import heronarts.p4lx.ui.component.UIKnob;
import storytime.lx.app.modulator.DashInputs;

public class UIDashInputs implements UIModulatorControls<DashInputs>  {
    private static final int TOP_PADDING = 4;

    public UIDashInputs() {
    }

    public void buildModulatorControls(LXStudio.UI ui, UIModulator uiModulator, DashInputs dashInputs) {
        uiModulator.setContentHeight(46.0F);
        uiModulator.setLayout(UI2dContainer.Layout.HORIZONTAL_GRID);
        uiModulator.setChildSpacing(2.0F);
        (new UIKnob(dashInputs.intensity)).setY(4.0F).addToContainer(uiModulator);
        (new UIKnob(dashInputs.vibe)).setY(4.0F).addToContainer(uiModulator);
        (new UIKnob(dashInputs.mood)).setY(4.0F).addToContainer(uiModulator);
        (new UIKnob(dashInputs.disco)).setY(4.0F).addToContainer(uiModulator);
        (new UIKnob(dashInputs.transition)).setY(4.0F).addToContainer(uiModulator);
        (new UIKnob(dashInputs.user)).setY(4.0F).addToContainer(uiModulator);

        (new UIButton(16.0F, 16.0F, dashInputs.auto)).setY(4.0F).addToContainer(uiModulator);
        (new UIButton(16.0F, 16.0F, dashInputs.effect)).setY(4.0F).addToContainer(uiModulator);

        (new UIButton(16.0F, 16.0F, dashInputs.trigger1).setTriggerable(true)).setY(4.0F).addToContainer(uiModulator);
        (new UIButton(16.0F, 16.0F, dashInputs.trigger2).setTriggerable(true)).setY(4.0F).addToContainer(uiModulator);
        (new UIButton(16.0F, 16.0F, dashInputs.trigger3).setTriggerable(true)).setY(4.0F).addToContainer(uiModulator);
        (new UIButton(16.0F, 16.0F, dashInputs.trigger4).setTriggerable(true)).setY(4.0F).addToContainer(uiModulator);
    }

    public static void register(LXStudio.UI ui) {
        ui.registry.addUIModulatorControls(UIDashInputs.class);
    }
}
