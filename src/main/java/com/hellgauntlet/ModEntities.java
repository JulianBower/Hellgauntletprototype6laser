package com.hellgauntlet;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HellGauntletMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<VoidOrbEntity>> VOID_ORB =
            ENTITIES.register("void_orb", () -> EntityType.Builder.<VoidOrbEntity>of(VoidOrbEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("void_orb"));

    // Purely visual black sphere that fills the carved-out hole and fades away.
    public static final DeferredHolder<EntityType<?>, EntityType<VoidSphereEntity>> VOID_SPHERE =
            ENTITIES.register("void_sphere", () -> EntityType.Builder.<VoidSphereEntity>of(VoidSphereEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .noSave()
                    .clientTrackingRange(12)
                    .updateInterval(20)
                    .build("void_sphere"));

    // The straight laser beam (visual + deletes entities it passes through).
    public static final DeferredHolder<EntityType<?>, EntityType<LaserBeamEntity>> LASER_BEAM =
            ENTITIES.register("laser_beam", () -> EntityType.Builder.<LaserBeamEntity>of(LaserBeamEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .noSave()
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("laser_beam"));
}
