package com.kolatra.gnome;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.SubTag;
import gregtech.api.interfaces.IItemBehaviour;
import gregtech.api.items.MetaBaseItem;

public class Enlarger extends MetaBaseItem {

    @SideOnly(Side.CLIENT)
    private IIcon enlargerIcon;

    public static final int TIER = 3; // HV
    public static final long MAX_CHARGE = 1_200_000L;
    public static final long EU_PER_RESIZE = 512L;

    public Enlarger() {
        super("gnome.enlarger");
        addItemBehavior(0, new BehaviourShrink());
    }

    @Override
    public Long[] getElectricStats(ItemStack stack) {
        if (stack.getItemDamage() != 0) return null;
        return new Long[] { MAX_CHARGE, GTValues.V[TIER], (long) TIER, -3L };
    }

    @Override
    public Long[] getFluidContainerStats(ItemStack stack) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        enlargerIcon = register.registerIcon("gnome:enlarger");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        return enlargerIcon;
    }

    private static class BehaviourShrink implements IItemBehaviour<MetaBaseItem> {

        @Override
        public ItemStack onItemRightClick(MetaBaseItem item, ItemStack stack, World world, EntityPlayer player) {
            resizePlayer(item, stack, player, world);
            return stack;
        }

        @Override
        public boolean onItemUse(MetaBaseItem item, ItemStack stack, EntityPlayer player, World world, int aX, int aY,
            int aZ, int ordinalSide, float hitX, float hitY, float hitZ) {
            resizePlayer(item, stack, player, world);
            return true;
        }

        private void resizePlayer(MetaBaseItem item, ItemStack stack, EntityPlayer player, World world) {
            if (!world.isRemote) {
                if (!SizeManager.canResize(player)) {
                    player.addChatComponentMessage(new ChatComponentText("Need more space to scale"));
                } else if (item.use(stack, Enlarger.EU_PER_RESIZE, player)) {
                    float scale = SizeManager.toggle(player);
                    ModMain.NETWORK.sendToAll(new SizePacket(player.getCommandSenderName(), scale));
                }
            }
        }

        @Override
        public boolean onLeftClickEntity(MetaBaseItem aItem, ItemStack aStack, EntityPlayer aPlayer, Entity aEntity) {
            return false;
        }

        @Override
        public boolean onItemUseFirst(MetaBaseItem aItem, ItemStack aStack, EntityPlayer player, World world, int aX,
            int aY, int aZ, ForgeDirection side, float hitX, float hitY, float hitZ) {
            return false;
        }

        @Override
        public List<String> getAdditionalToolTips(MetaBaseItem aItem, List<String> aList, ItemStack aStack) {
            aList.add(
                EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC
                    + StatCollector.translateToLocal("gt.gnome.enlarger.tooltip"));
            return aList;
        }

        @Override
        public void onUpdate(MetaBaseItem aItem, ItemStack aStack, World aWorld, Entity aPlayer, int aTimer,
            boolean aIsInHand) {

        }

        @Override
        public boolean isItemStackUsable(MetaBaseItem aItem, ItemStack aStack) {
            return true;
        }

        @Override
        public boolean canDispense(MetaBaseItem aItem, IBlockSource aSource, ItemStack aStack) {
            return false;
        }

        @Override
        public ItemStack onDispense(MetaBaseItem aItem, IBlockSource aSource, ItemStack aStack) {
            return null;
        }

        @Override
        public boolean hasProjectile(MetaBaseItem aItem, SubTag aProjectileType, ItemStack aStack) {
            return false;
        }

        @Override
        public EntityArrow getProjectile(MetaBaseItem aItem, SubTag aProjectileType, ItemStack aStack, World aWorld,
            double aX, double aY, double aZ) {
            return null;
        }

        @Override
        public EntityArrow getProjectile(MetaBaseItem aItem, SubTag aProjectileType, ItemStack aStack, World aWorld,
            EntityLivingBase aEntity, float aSpeed) {
            return null;
        }
    }
}
