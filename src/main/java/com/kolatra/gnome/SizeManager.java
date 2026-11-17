package com.kolatra.gnome;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;

import org.jetbrains.annotations.NotNull;

public final class SizeManager {

    private SizeManager() {}

    public static final float NORMAL = 1.0F;
    public static final float HALF = 0.5F;
    private static final String PERSISTED_TAG = "PlayerPersisted";
    private static final String SCALE_TAG = ModMain.MODID + ".scale";

    private static final Map<String, Float> CLIENT_SCALES = new ConcurrentHashMap<>();

    public static void setClientScale(String playerName, float scale) {
        CLIENT_SCALES.put(playerName, normalize(scale));
    }

    public static void clearAll() {
        CLIENT_SCALES.clear();
    }

    public static float get(@NotNull EntityPlayer player) {
        if (!player.worldObj.isRemote) {
            NBTTagCompound persisted = getPersistedData(player);
            if (persisted.hasKey(SCALE_TAG)) {
                return normalize(persisted.getFloat(SCALE_TAG));
            }

            return NORMAL;
        }

        Float scale = CLIENT_SCALES.get(player.getCommandSenderName());
        return scale == null ? NORMAL : scale;
    }

    public static float toggle(EntityPlayer player) {
        float scale = get(player) == NORMAL ? HALF : NORMAL;
        getPersistedData(player).setFloat(SCALE_TAG, scale);
        return scale;
    }

    public static void apply(EntityPlayer player) {
        float scale = get(player);
        float width = 0.6F * scale;
        float height = 1.8F * scale;

        if (player.width != width || player.height != height) {
            player.width = width;
            player.height = height;
            player.setPosition(player.posX, player.posY, player.posZ);
        }

        player.eyeHeight = player.getDefaultEyeHeight() - 1.62F * (1.0F - scale);
    }

    /**
     * Checks for a block above when downsized, because if we upsize we'll suffocate
     */
    public static boolean canResize(EntityPlayer player) {
        if (get(player) == NORMAL) {
            return true;
        }

        AxisAlignedBB currentBox = player.boundingBox;
        double centerX = (currentBox.minX + currentBox.maxX) * 0.5D;
        double centerZ = (currentBox.minZ + currentBox.maxZ) * 0.5D;
        double halfWidth = 0.3D;
        AxisAlignedBB normalSizeBox = AxisAlignedBB.getBoundingBox(
            centerX - halfWidth,
            currentBox.minY,
            centerZ - halfWidth,
            centerX + halfWidth,
            currentBox.minY + 1.8D,
            centerZ + halfWidth);

        return player.worldObj.getCollidingBoundingBoxes(player, normalSizeBox)
            .isEmpty();
    }

    public static void sendToPlayer(EntityPlayerMP joiningPlayer) {
        float joiningScale = SizeManager.get(joiningPlayer);
        SizePacket joiningPlayerPacket = new SizePacket(joiningPlayer.getCommandSenderName(), joiningScale);
        ModMain.NETWORK.sendTo(joiningPlayerPacket, joiningPlayer);

        for (EntityPlayerMP playerMP : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (playerMP == joiningPlayer) continue;

            ModMain.NETWORK.sendTo(new SizePacket(joiningPlayer.getCommandSenderName(), joiningScale), playerMP);
            ModMain.NETWORK
                .sendTo(new SizePacket(playerMP.getCommandSenderName(), SizeManager.get(playerMP)), joiningPlayer);
        }
    }

    private static float normalize(float scale) {
        return scale == HALF ? HALF : NORMAL;
    }

    private static NBTTagCompound getPersistedData(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        NBTTagCompound persisted;
        if (entityData.hasKey(PERSISTED_TAG)) {
            persisted = entityData.getCompoundTag(PERSISTED_TAG);
        } else {
            persisted = new NBTTagCompound();
            entityData.setTag(PERSISTED_TAG, persisted);
        }
        return persisted;
    }
}
