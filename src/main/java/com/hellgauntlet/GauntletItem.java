package com.hellgauntlet;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class GauntletItem extends Item {
    private static final String KEY_MODE = "mode";
    private static final String KEY_LASER = "laser";
    private static final String KEY_LASER_MODE = "laserMode";

    private static final int ORB_COOLDOWN = 20;
    private static final int LASER_COOLDOWN = 40;
    private static final int CONTINUOUS_COOLDOWN = 20;
    private static final float ORB_SPEED = 1.6F;
    public static final double LASER_RANGE = 64.0;

    public GauntletItem(Properties properties) {
        super(properties);
    }

    /** 5 attack damage and 1.8 attack speed, as shown in the tooltip (base damage 1, base speed 4). */
    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 4.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.2, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    /** Lets the item be enchanted at an enchanting table (mending, sharpness and so on). */
    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    /** Like a nether star: dropped copies are not destroyed by explosions (they just get blown away). */
    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return super.canBeHurtBy(stack, source) && !source.is(DamageTypeTags.IS_EXPLOSION);
    }

    // ---- stored settings (kept on the item itself) ----

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void edit(ItemStack stack, Consumer<CompoundTag> change) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, change);
    }

    public static boolean isLaser(ItemStack stack) {
        return data(stack).getBoolean(KEY_LASER);
    }

    public static BlastMode getBlastMode(ItemStack stack) {
        BlastMode[] all = BlastMode.values();
        return all[Math.floorMod(data(stack).getInt(KEY_MODE), all.length)];
    }

    public static LaserMode getLaserMode(ItemStack stack) {
        LaserMode[] all = LaserMode.values();
        return all[Math.floorMod(data(stack).getInt(KEY_LASER_MODE), all.length)];
    }

    /** True while this stack is set to the continuous laser. */
    public static boolean isContinuousLaser(ItemStack stack) {
        return isLaser(stack) && getLaserMode(stack).continuous;
    }

    /** Called on the server when the player presses X. */
    public static void toggleLaser(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GauntletItem)) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof GauntletItem)) {
                return;
            }
        }
        boolean nowLaser = !isLaser(stack);
        edit(stack, tag -> tag.putBoolean(KEY_LASER, nowLaser));
        player.displayClientMessage(Component.literal(nowLaser
                ? "Laser mode: " + getLaserMode(stack).displayName
                : "Orb mode: " + getBlastMode(stack).displayName), true);
    }

    private static void cycleMode(ItemStack stack, Player player) {
        if (isLaser(stack)) {
            int next = Math.floorMod(data(stack).getInt(KEY_LASER_MODE) + 1, LaserMode.values().length);
            edit(stack, tag -> tag.putInt(KEY_LASER_MODE, next));
            player.displayClientMessage(Component.literal("Laser mode: " + getLaserMode(stack).displayName), true);
        } else {
            int next = Math.floorMod(data(stack).getInt(KEY_MODE) + 1, BlastMode.values().length);
            edit(stack, tag -> tag.putInt(KEY_MODE, next));
            player.displayClientMessage(Component.literal("Orb mode: " + getBlastMode(stack).displayName), true);
        }
    }

    // ---- using the item ----

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                cycleMode(stack, player);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (isContinuousLaser(stack)) {
            // Hold right click: the beam stays on while the item is being used
            player.startUsingItem(hand);
            if (!level.isClientSide) {
                LaserMode mode = getLaserMode(stack);
                LaserBeamEntity beam = new LaserBeamEntity(ModEntities.LASER_BEAM.get(), level);
                beam.setPos(player.getX(), player.getEyeY(), player.getZ());
                beam.setupContinuous(player.getUUID(), mode.ballRadius, mode.durabilityCost);
                level.addFreshEntity(beam);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.4F);
                // first payment for the beam, the beam itself charges again every 2.5 seconds
                stack.hurtAndBreak(mode.durabilityCost, player, LivingEntity.getSlotForHand(hand));
            }
            return InteractionResultHolder.consume(stack);
        }

        if (!level.isClientSide) {
            if (isLaser(stack)) {
                fireLaser(level, player, hand, stack, getLaserMode(stack));
            } else {
                fireOrb(level, player, hand, stack, getBlastMode(stack));
            }
        }

        player.getCooldowns().addCooldown(this, isLaser(stack) ? LASER_COOLDOWN : ORB_COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // effectively "as long as you hold the button"
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, CONTINUOUS_COOLDOWN);
        }
    }

    private void fireOrb(Level level, Player player, InteractionHand hand, ItemStack stack, BlastMode mode) {
        VoidOrbEntity orb = new VoidOrbEntity(level, player);
        orb.setMode(mode);
        orb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, ORB_SPEED, 0.0F);
        level.addFreshEntity(orb);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8F, 0.6F);
        stack.hurtAndBreak(mode.durabilityCost, player, LivingEntity.getSlotForHand(hand));
    }

    private void fireLaser(Level level, Player player, InteractionHand hand, ItemStack stack, LaserMode mode) {
        Vec3 look = player.getLookAngle();
        Vec3 start = player.getEyePosition().add(look.scale(0.6)).add(0.0, -0.25, 0.0);
        Vec3 end = start.add(look.scale(LASER_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        boolean hitBlock = hit.getType() == HitResult.Type.BLOCK;
        double length = hitBlock ? start.distanceTo(hit.getLocation()) : LASER_RANGE;

        LaserBeamEntity beam = new LaserBeamEntity(ModEntities.LASER_BEAM.get(), level);
        beam.setPos(start.x, start.y, start.z);
        beam.setYRot(player.getYRot());
        beam.setXRot(player.getXRot());
        beam.setup((float) length, player.getUUID(), mode.ballRadius, hitBlock);
        level.addFreshEntity(beam);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.4F);
        stack.hurtAndBreak(mode.durabilityCost, player, LivingEntity.getSlotForHand(hand));
    }
}
