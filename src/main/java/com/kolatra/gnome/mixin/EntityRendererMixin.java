package com.kolatra.gnome.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.kolatra.gnome.SizeManager;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @ModifyArg(
        method = "orientCamera(F)V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V", remap = false, ordinal = 4),
        index = 1)
    private float gnome$useScaledPlayerCameraHeight(float cameraOffset) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity viewEntity = minecraft.renderViewEntity;
        if (viewEntity instanceof EntityPlayer && SizeManager.get((EntityPlayer) viewEntity) == SizeManager.HALF) {
            // lowers the camera based to match where the player's head is when smaller
            // 1.7.10 hardcodes the offset so we have to be goofy
            return 1.62F * (1.0F - SizeManager.get((EntityPlayer) viewEntity));
        }
        return cameraOffset;
    }
}
