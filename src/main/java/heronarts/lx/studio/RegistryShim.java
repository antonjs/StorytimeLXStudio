package heronarts.lx.studio;

import heronarts.lx.studio.ui.modulation.UIModulatorControls;

public class RegistryShim {
    public RegistryShim() { }

    public static void registerUIModulator(LXStudio.UI ui, Class<? extends UIModulatorControls> controls) {
        ui.registry.addUIModulatorControls(controls);
    }
}
