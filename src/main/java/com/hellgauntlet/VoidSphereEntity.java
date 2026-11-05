package com.hellgauntlet;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/** Visual only: a black sphere with a jagged red edge. It grows fast, holds a moment, then fades out. */
public class VoidSphereEntity extends Entity {
    public static final int GROW_TICKS = 4;
    public static final int HOLD_TICKS = 2;
    public static final int FADE_TICKS = 8;
    public static final int TOTAL_TICKS = GROW_TICKS + HOLD_TICKS + FADE_TICKS;

    private static final EntityDataAccessor<Integer> RADIUS =
            SynchedEntityData.defineId(VoidSphereEntity.class, EntityDataSerializers.INT);
    private static final DustParticleOptions RED = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 0.8F);

    public VoidSphereEntity(EntityType<? extends VoidSphereEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public int getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setRadius(int radius) {
        this.entityData.set(RADIUS, radius);
    }

    /** Radius of the drawn sphere after the given age in ticks (eases out quickly). */
    public static float visualRadius(int maxRadius, float age) {
        float x = Math.min(1.0F, age / GROW_TICKS);
        float eased = 1.0F - (1.0F - x) * (1.0F - x);
        return maxRadius * eased + 0.5F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.tickCount <= TOTAL_TICKS - 2) {
                spawnJaggedEdge();
            }
        } else if (this.tickCount > TOTAL_TICKS) {
            this.discard();
        }
    }

    /** Lots of red lightning: zigzags hugging the edge plus jagged spikes shooting outward. */
    private void spawnJaggedEdge() {
        float radius = visualRadius(getRadius(), this.tickCount);

        // zigzag bolts along the surface
        for (int bolt = 0; bolt < 22; bolt++) {
            double ux = this.random.nextGaussian();
            double uy = this.random.nextGaussian();
            double uz = this.random.nextGaussian();
            for (int step = 0; step < 9; step++) {
                double len = Math.sqrt(ux * ux + uy * uy + uz * uz);
                if (len < 1.0E-3) {
                    break;
                }
                ux /= len;
                uy /= len;
                uz /= len;
                double r = radius + (this.random.nextDouble() - 0.5) * 1.1;
                this.level().addParticle(RED, this.getX() + ux * r, this.getY() + uy * r, this.getZ() + uz * r, 0, 0, 0);
                ux += this.random.nextGaussian() * 0.2;
                uy += this.random.nextGaussian() * 0.2;
                uz += this.random.nextGaussian() * 0.2;
            }
        }

        // spikes reaching out past the edge
        for (int spike = 0; spike < 14; spike++) {
            double ux = this.random.nextGaussian();
            double uy = this.random.nextGaussian();
            double uz = this.random.nextGaussian();
            double len = Math.sqrt(ux * ux + uy * uy + uz * uz);
            if (len < 1.0E-3) {
                continue;
            }
            ux /= len;
            uy /= len;
            uz /= len;
            double jx = 0.0;
            double jy = 0.0;
            double jz = 0.0;
            for (int k = 0; k < 6; k++) {
                double r = radius - 0.2 + k * 0.3;
                this.level().addParticle(RED,
                        this.getX() + ux * r + jx, this.getY() + uy * r + jy, this.getZ() + uz * r + jz, 0, 0, 0);
                jx += (this.random.nextDouble() - 0.5) * 0.25;
                jy += (this.random.nextDouble() - 0.5) * 0.25;
                jz += (this.random.nextDouble() - 0.5) * 0.25;
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
