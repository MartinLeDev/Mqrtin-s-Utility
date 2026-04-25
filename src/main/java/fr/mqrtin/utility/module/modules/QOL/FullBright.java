package fr.mqrtin.utility.module.modules.QOL;

import fr.mqrtin.utility.event.EventTarget;
import fr.mqrtin.utility.event.events.TickEvent;
import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.module.impl.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class FullBright extends Module {
    private float prevGamma = Float.NaN;
    private boolean appliedNightVision = false;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"GAMMA", "EFFECT"});

    public FullBright() {
        super("FullBright", ModuleCategory.QOL, true);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getStage() == TickEvent.Stage.END) {
            if (Minecraft.getMinecraft().thePlayer == null) return;

            switch (this.mode.getValue()) {
                case 0:
                    // Utiliser la valeur max pour gamma (généralement 2.0F ou plus haut)
                    Minecraft.getMinecraft().gameSettings.gammaSetting = 1000.0F;
                    break;
                case 1:
                    Minecraft.getMinecraft().thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 25940, 0));
                    break;
            }
        }
    }

    @Override
    public void onEnable() {
        if (Minecraft.getMinecraft().gameSettings == null) return;

        switch (this.mode.getValue()) {
            case 0:
                this.prevGamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
                break;
            case 1:
                this.appliedNightVision = true;
                break;
        }
    }

    @Override
    public void onDisable() {
        if (!Float.isNaN(this.prevGamma)) {
            Minecraft.getMinecraft().gameSettings.gammaSetting = this.prevGamma;
            this.prevGamma = Float.NaN;
        }
        if (this.appliedNightVision) {
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.removePotionEffectClient(Potion.nightVision.id);
            }
            this.appliedNightVision = false;
        }
    }

    @Override
    public void verifyValue(String mode) {
        if (this.isEnabled()) {
            this.onDisable();
            this.onEnable();
        }
    }

}
