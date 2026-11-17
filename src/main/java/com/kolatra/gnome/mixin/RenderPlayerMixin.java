package com.kolatra.gnome.mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kolatra.gnome.SizeManager;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin {

    @Inject(method = "preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V", at = @At("HEAD"))
    private void gnome$scalePlayerModel(AbstractClientPlayer player, float partialTicks, CallbackInfo callbackInfo) {
        float scale = SizeManager.get(player);
        if (scale == SizeManager.HALF) {
            GL11.glScalef(scale, scale, scale);
        }
    }
}
