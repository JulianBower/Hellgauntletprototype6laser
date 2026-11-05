package com.hellgauntlet;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** Raises the arm straight up while casting: while holding right click (continuous beam) and right after a shot. */
public class GauntletClientExtensions implements IClientItemExtensions {
    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        boolean casting = entity.isUsingItem() && entity.getUsedItemHand() == hand;
        if (!casting && entity instanceof Player player) {
            // arm stays up for roughly the first 40% of the cooldown after a shot
            casting = player.getCooldowns().getCooldownPercent(stack.getItem(), 0.0F) > 0.6F;
        }
        return casting ? HumanoidModel.ArmPose.THROW_SPEAR : null;
    }
}
