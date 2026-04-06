package fr.mqrtin.utility.mixin;

import fr.mqrtin.utility.event.EventManager;
import fr.mqrtin.utility.event.events.Render3DEvent;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void onRenderWorld(float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
        EventManager.call(new Render3DEvent(partialTicks));
    }
}
