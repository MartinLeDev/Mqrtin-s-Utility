package fr.mqrtin.utility.module.modules.settings;

import fr.mqrtin.utility.gui.clickgui.ClickGui;
import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.Module;
import net.minecraft.client.Minecraft;

public class TabOverlay extends Module {

    public TabOverlay() {
        super("ClickGUI", ModuleCategory.SETTINGS, true);
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.displayGuiScreen(new ClickGui());
        }
        setEnabled(false);
    }

}