package com.hellgauntlet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Expanding spheres of nothingness. A blast grows quickly and deletes everything new inside it:
 * entities vanish (no loot, no death), blocks vanish (no drops).
 * The ender dragon is the exception: it is sent into its normal death so the fight ends properly.
 */
public class VoidBlasts {
    private static final List<Blast> BLASTS = new ArrayList<>();
    private static final List<Pending> PENDING = new ArrayList<>();

    private static class Blast {
        final ServerLevel level;
        final Vec3 center;
        final UUID owner;
        final int maxRadius;
        final boolean deleteBlocks;
        final int step;
        int radius = -1; // last shell that was processed

        Blast(ServerLevel level, Vec3 center, UUID owner, int maxRadius, boolean deleteBlocks) {
            this.level = level;
            this.center = center;
            this.owner = owner;
            this.maxRadius = maxRadius;
            this.deleteBlocks = deleteBlocks;
            // The sphere should be fully grown in about 4 ticks, so big blasts process several shells per tick
            this.step = Math.max(1, (maxRadius + 3) / 4);
        }
    }

    private static class Pending {
        final ServerLevel level;
        final Vec3 center;
        final UUID owner;
        final int radius;
        int delay;

        Pending(ServerLevel level, Vec3 center, UUID owner, int radius, int delay) {
            this.level = level;
            this.center = center;
            this.owner = owner;
            this.radius = radius;
            this.delay = delay;
        }
    }

    public static void start(ServerLevel level, Vec3 center, Entity owner, int radius, boolean deleteBlocks) {
        start(level, center, owner == null ? null : owner.getUUID(), radius, deleteBlocks);
    }

    public static void start(ServerLevel level, Vec3 center, UUID owner, int radius, boolean deleteBlocks) {
        start(level, center, owner, radius, deleteBlocks, true);
    }

    public static void start(ServerLevel level, Vec3 center, UUID owner, int radius, boolean deleteBlocks, boolean playSound) {
        BLASTS.add(new Blast(level, center, owner, radius, deleteBlocks));

        // The visual black sphere that fills the hole and fades away
        VoidSphereEntity sphere = new VoidSphereEntity(ModEntities.VOID_SPHERE.get(), level);
        sphere.setPos(center.x, center.y, center.z);
        sphere.setRadius(radius);
        level.addFreshEntity(sphere);

        if (playSound) {
            level.playSound(null, center.x, center.y, center.z,
                    SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 1.5F, 0.5F + 0.1F * radius);
        }
    }

    /** Several blasts in a row at the same spot, each a little bigger, so the ball seems to pulsate. */
    public static void startPulsing(ServerLevel level, Vec3 center, UUID owner, int baseRadius, int pulses, int interval) {
        for (int i = 0; i < pulses; i++) {
            PENDING.add(new Pending(level, center, owner, baseRadius + i, i * interval));
        }
    }

    /** Blocks the void can't delete (bedrock, portal frames, barriers...). Same rule the blast itself uses. */
    public static boolean isUnbreakable(Level level, BlockPos pos, BlockState state) {
        return !state.isAir() && state.getDestroySpeed(level, pos) < 0.0F;
    }

    public static boolean canErase(Entity e, UUID owner) {
        return !(e instanceof VoidOrbEntity)
                && !(e instanceof VoidSphereEntity)
                && !(e instanceof LaserBeamEntity)
                && !(e instanceof EnderDragonPart)
                && !e.getUUID().equals(owner);
    }

    public static void erase(ServerLevel level, Entity e) {
        if (e instanceof EnderDragon dragon) {
            killDragon(dragon);
        } else if (e instanceof ServerPlayer player) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
            }
        } else {
            e.discard();
        }
    }

    /**
     * The dragon only takes damage from players and explosions, and deleting it would leave the fight
     * stuck (boss bar stays, no exit portal). So we put it straight into its dying phase: it flies to the
     * portal spot, dies normally, and ends the fight.
     */
    private static void killDragon(EnderDragon dragon) {
        if (dragon.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
            dragon.getPhaseManager().setPhase(EnderDragonPhase.DYING);
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Iterator<Pending> pending = PENDING.iterator();
        while (pending.hasNext()) {
            Pending p = pending.next();
            if (p.level != level) {
                continue;
            }
            if (p.delay-- <= 0) {
                pending.remove();
                start(level, p.center, p.owner, p.radius, true);
            }
        }

        Iterator<Blast> it = BLASTS.iterator();
        while (it.hasNext()) {
            Blast blast = it.next();
            if (blast.level != level) {
                continue;
            }
            tickBlast(blast);
            if (blast.radius >= blast.maxRadius) {
                it.remove();
            }
        }
    }

    private static double distSqToBox(AABB bb, Vec3 p) {
        double x = Mth.clamp(p.x, bb.minX, bb.maxX) - p.x;
        double y = Mth.clamp(p.y, bb.minY, bb.maxY) - p.y;
        double z = Mth.clamp(p.z, bb.minZ, bb.maxZ) - p.z;
        return x * x + y * y + z * z;
    }

    private static void tickBlast(Blast blast) {
        ServerLevel level = blast.level;
        final Vec3 c = blast.center;
        final int from = blast.radius + 1;
        final int to = Math.min(blast.maxRadius, blast.radius + blast.step);
        final double reach = to + 0.5;

        // Entities touching the sphere (judged by their hitbox, so big mobs like the dragon count)
        AABB area = new AABB(c, c).inflate(reach + 8.0);
        List<Entity> targets = level.getEntities((Entity) null, area, e -> {
            if (!canErase(e, blast.owner)) {
                return false;
            }
            AABB bb = e.getBoundingBox();
            if (e instanceof EnderDragon) {
                bb = bb.inflate(4.0);
            }
            return distSqToBox(bb, c) <= reach * reach;
        });
        for (Entity e : targets) {
            erase(level, e);
        }

        // Blocks on the new shells of the sphere
        if (blast.deleteBlocks) {
            BlockPos origin = BlockPos.containing(c);
            double outer = reach * reach;
            double inner = from == 0 ? -1.0 : (from - 0.5) * (from - 0.5);
            for (int dx = -to; dx <= to; dx++) {
                for (int dy = -to; dy <= to; dy++) {
                    for (int dz = -to; dz <= to; dz++) {
                        double d2 = dx * dx + dy * dy + dz * dz;
                        if (d2 > outer || d2 <= inner) {
                            continue;
                        }
                        BlockPos pos = origin.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
                            continue; // skip air and unbreakable blocks like bedrock
                        }
                        // Remove the block entity first so chests etc. don't drop their contents
                        if (level.getBlockEntity(pos) != null) {
                            level.removeBlockEntity(pos);
                        }
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
                    }
                }
            }
        }

        blast.radius = to;
    }
}
