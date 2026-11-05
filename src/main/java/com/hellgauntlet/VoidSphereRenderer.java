package com.hellgauntlet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class VoidSphereRenderer extends EntityRenderer<VoidSphereEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellGauntletMod.MODID, "textures/entity/void_sphere.png");
    private static final int STACKS = 14;
    private static final int SLICES = 20;

    public VoidSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(VoidSphereEntity entity, Frustum frustum, double x, double y, double z) {
        return true; // the sphere is much bigger than the entity's tiny hitbox
    }

    @Override
    public void render(VoidSphereEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        float age = entity.tickCount + partialTick;
        float radius = VoidSphereEntity.visualRadius(entity.getRadius(), age);

        float opacity = 1.0F;
        float fadeStart = VoidSphereEntity.GROW_TICKS + VoidSphereEntity.HOLD_TICKS;
        if (age > fadeStart) {
            opacity = 1.0F - (age - fadeStart) / VoidSphereEntity.FADE_TICKS;
        }
        int alpha = (int) (Math.max(0.0F, Math.min(1.0F, opacity)) * 255.0F);
        if (alpha <= 0) {
            return;
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        for (int i = 0; i < STACKS; i++) {
            double phi0 = Math.PI * i / STACKS;
            double phi1 = Math.PI * (i + 1) / STACKS;
            for (int j = 0; j < SLICES; j++) {
                double t0 = 2.0 * Math.PI * j / SLICES;
                double t1 = 2.0 * Math.PI * (j + 1) / SLICES;
                vertex(vc, pose, radius, phi0, t0, alpha);
                vertex(vc, pose, radius, phi0, t1, alpha);
                vertex(vc, pose, radius, phi1, t1, alpha);
                vertex(vc, pose, radius, phi1, t0, alpha);
            }
        }

        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float radius, double phi, double theta, int alpha) {
        float nx = (float) (Math.sin(phi) * Math.cos(theta));
        float ny = (float) Math.cos(phi);
        float nz = (float) (Math.sin(phi) * Math.sin(theta));
        vc.addVertex(pose, nx * radius, ny * radius, nz * radius)
                .setColor(0, 0, 0, alpha)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidSphereEntity entity) {
        return TEXTURE;
    }
}
