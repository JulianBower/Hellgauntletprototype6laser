package com.hellgauntlet;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class VoidOrbEntity extends ThrowableItemProjectile {
    private static final DustParticleOptions RED_SPARK = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 0.4F);
    private static final int MAX_LIFETIME = 200;

    private BlastMode mode = BlastMode.LARGE;

    public VoidOrbEntity(EntityType<? extends VoidOrbEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public VoidOrbEntity(Level level, LivingEntity owner) {
        super(ModEntities.VOID_ORB.get(), owner, level);
        this.setNoGravity(true);
    }

    public void setMode(BlastMode mode) {
        this.mode = mode;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.VOID_ORB.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            spawnLightning();
        } else if (this.tickCount > MAX_LIFETIME) {
            this.discard();
        }
    }

    /** Short, jagged red bolts that flicker around the orb every tick. */
    private void spawnLightning() {
        for (int bolt = 0; bolt < 3; bolt++) {
            double dx = this.random.nextGaussian();
            double dy = this.random.nextGaussian();
            double dz = this.random.nextGaussian();
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1.0E-3) {
                continue;
            }
            dx /= len;
            dy /= len;
            dz /= len;
            double x = this.getX() + dx * 0.2;
            double y = this.getY() + 0.125 + dy * 0.2;
            double z = this.getZ() + dz * 0.2;
            for (int step = 0; step < 5; step++) {
                this.level().addParticle(RED_SPARK, x, y, z, 0, 0, 0);
                x += dx * 0.1 + (this.random.nextDouble() - 0.5) * 0.14;
                y += dy * 0.1 + (this.random.nextDouble() - 0.5) * 0.14;
                z += dz * 0.1 + (this.random.nextDouble() - 0.5) * 0.14;
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 center = result.getLocation();
        int radius = this.mode.radius;
        boolean blocks = this.mode.deleteBlocks;

        if (this.mode == BlastMode.ENTITY_ONLY) {
            if (result instanceof EntityHitResult entityHit) {
                // Just big enough to cover whatever was hit
                Entity target = entityHit.getEntity();
                radius = (int) Math.ceil(Math.max(target.getBbWidth(), target.getBbHeight()) / 2.0) + 1;
                radius = Mth.clamp(radius, 2, 12);
                center = target.getBoundingBox().getCenter();
            } else {
                radius = 1; // hit a block: only a tiny puff
            }
        }

        VoidBlasts.start(serverLevel, center, this.getOwner(), radius, blocks);
        this.discard();
    }
}
